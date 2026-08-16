package moe.shizuku.manager.adb

import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings

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

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(2002)

        val scroll = android.widget.ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 0, 48, 0)
            setBackgroundColor(Color.parseColor("#80000000"))
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
            setOnClickListener { }
        }
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

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
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(Color.parseColor("#FFF0F6"))
                setStroke(2, Color.parseColor("#FFD6E8"))
            }
        }.also { layout.addView(it) }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            adbMdns = AdbMdns(this, AdbMdns.TLS_CONNECT) { p -> port.postValue(p) }
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

        if (code.isEmpty()) { showStatus("请输入配对码", Color.RED); return }
        if (portStr.isEmpty()) { showStatus("请输入端口", Color.RED); return }
        val portNum = portStr.toIntOrNull() ?: -1
        if (portNum <= 0) { showStatus("端口无效", Color.RED); return }

        confirmBtn.isEnabled = false
        confirmBtn.text = "配对中..."
        showStatus("正在配对...", PINK)

        CoroutineScope(Dispatchers.IO).launch {
            val success = try {
                val key = AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
                val client = AdbPairingClient("127.0.0.1", portNum, code, key)
                client.start()
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    showStatus("配对成功！可以点击激活了", Color.parseColor("#4CAF50"))
                    updatePairNotification(true)
                    android.os.Handler(mainLooper).postDelayed({ finish() }, 1500)
                } else {
                    showStatus("配对失败，请检查配对码和端口后重试", Color.RED)
                    updatePairNotification(false)
                    confirmBtn.isEnabled = true
                    confirmBtn.text = "确认配对"
                }
            }
        }
    }

    private fun updatePairNotification(success: Boolean) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "shizlite_service")
            .setContentTitle(if (success) "配对成功" else "配对失败")
            .setContentText(if (success) "可以点击激活了" else "请重试")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(0xFFFF69B4.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(2002, notification)
    }

    private fun showStatus(msg: String, color: Int) {
        statusText.text = msg
        statusText.setTextColor(color)
        statusText.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::adbMdns.isInitialized) adbMdns.stop()
    }
}
