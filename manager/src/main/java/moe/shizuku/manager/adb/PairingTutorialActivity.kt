package moe.shizuku.manager.adb

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class PairingTutorialActivity : AppCompatActivity() {

    private val PINK = Color.parseColor("#FF69B4")
    private var floatingWindow: FloatingPairingWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#FFF0F6"))
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        scroll.addView(root)

        TextView(this).apply {
            text = "无线调试配对教程"
            textSize = 22f
            setPadding(0, 0, 0, 24)
            setTextColor(Color.BLACK)
        }.also { root.addView(it) }

        val steps = listOf(
            "1. 打开系统设置 → 关于手机 → 连续点击「版本号」7次，开启开发者选项" to "返回设置主界面，进入「系统和更新」→「开发人员选项」",
            "2. 在开发者选项中，开启「无线调试」开关" to "如果弹出提示，点击「确定」允许无线调试",
            "3. 点击「使用配对码配对设备」" to "系统会弹出一个对话框，显示6位配对码和端口号",
            "4. 记下显示的6位配对码和端口号" to "配对码是6位数字，端口是5位数字",
            "5. 回到 shizLite，点击下方按钮，在悬浮窗中输入配对码完成配对" to "配对成功后，「激活」按钮会变为可点击状态"
        )

        for ((title, desc) in steps) {
            val card = CardView(this).apply {
                radius = 16f
                cardElevation = 2f
                setContentPadding(32, 24, 32, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }
                setCardBackgroundColor(Color.WHITE)
            }
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            TextView(this).apply {
                text = title
                textSize = 15f
                setPadding(0, 0, 0, 8)
                setTextColor(Color.BLACK)
            }.also { col.addView(it) }
            TextView(this).apply {
                text = desc
                textSize = 13f
                setTextColor(Color.parseColor("#666666"))
            }.also { col.addView(it) }
            card.addView(col)
            root.addView(card)
        }

        // 打开系统设置按钮
        Button(this).apply {
            text = "打开系统开发者选项"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(PINK)
            }
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16; bottomMargin = 16 }
            setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(this@PairingTutorialActivity, "无法打开开发者选项", Toast.LENGTH_SHORT).show()
                }
            }
        }.also { root.addView(it) }

        // 开始配对按钮
        Button(this).apply {
            text = "我已获取配对码，开始配对"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.parseColor("#E91E63"))
            }
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }
            setOnClickListener { startFloatingPairing() }
        }.also { root.addView(it) }

        setContentView(scroll)
    }

    private fun startFloatingPairing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                return
            }
        }
        floatingWindow = FloatingPairingWindow(this)
        floatingWindow?.show()
        Toast.makeText(this, "悬浮窗已显示，输入配对码完成配对", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingWindow?.dismiss()
    }
}
