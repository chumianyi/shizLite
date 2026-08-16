package moe.shizuku.manager.adb

import android.app.NotificationManager
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import moe.shizuku.manager.R

class AdbPairingActivity : AppCompatActivity() {

    private lateinit var codeInput: EditText
    private lateinit var portInput: EditText
    private lateinit var statusText: TextView
    private lateinit var confirmBtn: Button
    private lateinit var adbMdns: AdbMdns
    private val port = MutableLiveData<Int>()
    private val PINK = Color.parseColor("#FF69B4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 取消配对通知
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(2002)

        val scroll = android.widget.ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 0, 48, 0)
            setOnClickListener { finish() }
        }
        scroll.addView(root)

        val card = CardView(this).apply {
            radius = 24f
            cardElevation = 8f
            setContentPadding(48, 48, 48, 48)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { /* 阻止点击穿透 */ }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        TextView(this).apply {
            text = "无线调试配对"
            textSize = 20f
            setPadding(0, 0, 0, 16)
            setTextColor(Color.BLACK)
        }.also { layout.addView(it) }

        TextView(this).apply {
            text = "请在系统设置 → 开发者选项 → 无线调试中，点击「使用配对码配对设备」，将显示的配对码和端口填入下方"
            textSize = 13f
            setPadding(0, 0, 0, 24)
            setTextColor(Color.parseColor("#666666"))
        }.also { layout.addView(it) }

        // 配对码输入
        TextView(this).apply {
            text = "配对码"
            textSize = 13f
            setPadding(0, 0, 0, 8)
            setTextColor(Color.parseColor("#888888"))
        }.also { layout.addView(it) }
        codeInput = EditText(this).apply {
            hint = "6 位配对码"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            maxLines = 1
            setPadding(32, 24, 32, 24)
            setBackgroundResource(android.R.color.transparent)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(Color.parseColor("#FFF0F6"))
                setStroke(2, Color.parseColor("#FFD6E8"))
            }
        }.also { layout.addView(it) }

        // 端口输入
        TextView(this).apply {
            text = "端口"
            textSize = 13f
            setPadding(0, 24, 0, 8)
            setTextColor(Color.parseColor("#888888"))
        }.also { layout.addView(it) }
        portInput = EditText(this).apply {
            hint = "端口号（自动检测）"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            maxLines = 1
            setPadding(32, 24, 32, 24)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(Color.parseColor("#FFF0F6"))
                setStroke(2, Color.parseColor("#FFD6E8"))
            }
        }.also { layout.addView(it) }

        statusText = TextView(this).apply {
            textSize = 13f
            setPadding(0, 16, 0, 0)
            visibility = View.GONE
        }.also { layout.addView(it) }

        // 按钮行
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }
        Button(this).apply {
            text = "取消"
            setTextColor(Color.parseColor("#888888"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.parseColor("#F0F0F0"))
            }
            setPadding(32, 20, 32, 20)
            setOnClickListener { finish() }
        }.also { btnRow.addView(it, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 16 }) }

        confirmBtn = Button(this).apply {
            text = "确认配对"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(PINK)
            }
            setPadding(32, 20, 32, 20)
            setOnClickListener { doPairing() }
        }.also { btnRow.addView(it, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)) }
        layout.addView(btnRow)

        card.addView(layout)
        root.addView(card)
        setContentView(scroll)

        // 启动 mDNS 自动发现端口
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            adbMdns = AdbMdns(this, AdbMdns.TLS_CONNECT) { p ->
                port.postValue(p)
            }
            adbMdns.start()
            port.observe(this) { p ->
                if (p > 0) {
                    portInput.setText(p.toString())
                    showStatus("已检测到配对端口：$p", Color.parseColor("#4CAF50"))
                }
            }
        }
    }

    private fun doPairing() {
        val code = codeInput.text.toString().trim()
        val portStr = portInput.text.toString().trim()

        if (code.isEmpty()) {
            showStatus("请输入配对码", Color.RED)
            return
        }
        if (portStr.isEmpty()) {
            showStatus("请输入端口", Color.RED)
            return
        }

        val portNum = portStr.toIntOrNull() ?: -1
        if (portNum <= 0) {
            showStatus("端口无效", Color.RED)
            return
        }

        confirmBtn.isEnabled = false
        confirmBtn.text = "配对中..."

        // 通过 AdbPairingService 进行配对
        try {
            val intent = AdbPairingService::class.java.let { cls ->
                Intent(this, cls).apply {
                    action = "reply"
                    putExtra("paring_code", portNum)
                }
            }
            // 构造 RemoteInput 结果
            val remoteInput = RemoteInput.Builder("paring_code").build()
            val resultsIntent = Intent()
            RemoteInput.addResultsToIntent(arrayOf(remoteInput), resultsIntent, Bundle().apply {
                putCharSequence("paring_code", code)
            })
            intent.putExtras(resultsIntent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            showStatus("配对请求已发送，请稍候...", PINK)
            // 等待片刻后关闭，让服务处理配对
            android.os.Handler(mainLooper).postDelayed({ finish() }, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
            showStatus("配对失败：${e.message}", Color.RED)
            confirmBtn.isEnabled = true
            confirmBtn.text = "确认配对"
        }
    }

    private fun showStatus(msg: String, color: Int) {
        statusText.text = msg
        statusText.setTextColor(color)
        statusText.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::adbMdns.isInitialized) {
            adbMdns.stop()
        }
    }
}
