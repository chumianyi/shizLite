package moe.shizuku.manager.adb

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.ShizukuSettings

class FloatingPairingWindow(private val context: Context) {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val PINK = Color.parseColor("#FF69B4")

    fun show() {
        if (floatingView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val card = createPairingCard()
        floatingView = card
        windowManager?.addView(card, layoutParams)

        // 拖动
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        card.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(card, layoutParams)
                    true
                }
                else -> false
            }
        }
    }

    fun dismiss() {
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
    }

    private fun createPairingCard(): View {
        val card = CardView(context).apply {
            radius = 24f
            cardElevation = 16f
            setContentPadding(48, 48, 48, 48)
            setCardBackgroundColor(Color.WHITE)
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            minimumWidth = 700
        }

        TextView(context).apply {
            text = "无线调试配对"
            textSize = 20f
            setPadding(0, 0, 0, 16)
            setTextColor(Color.BLACK)
        }.also { layout.addView(it) }

        TextView(context).apply {
            text = "输入系统无线调试中显示的配对码和端口"
            textSize = 13f
            setPadding(0, 0, 0, 24)
            setTextColor(Color.parseColor("#666666"))
        }.also { layout.addView(it) }

        TextView(context).apply {
            text = "配对码"
            textSize = 13f
            setPadding(0, 0, 0, 8)
            setTextColor(Color.parseColor("#888888"))
        }.also { layout.addView(it) }
        val codeInput = EditText(context).apply {
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

        TextView(context).apply {
            text = "端口"
            textSize = 13f
            setPadding(0, 24, 0, 8)
            setTextColor(Color.parseColor("#888888"))
        }.also { layout.addView(it) }
        val portInput = EditText(context).apply {
            hint = "端口号"
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

        val statusText = TextView(context).apply {
            textSize = 13f
            setPadding(0, 16, 0, 0)
            visibility = View.GONE
        }.also { layout.addView(it) }

        val btnRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }
        Button(context).apply {
            text = "关闭"
            setTextColor(Color.parseColor("#888888"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.parseColor("#F0F0F0"))
            }
            setPadding(32, 20, 32, 20)
            setOnClickListener { dismiss() }
        }.also { btnRow.addView(it, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 16 }) }

        val confirmBtn = Button(context).apply {
            text = "确认配对"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(PINK)
            }
            setPadding(32, 20, 32, 20)
            setOnClickListener {
                val code = codeInput.text.toString().trim()
                val portStr = portInput.text.toString().trim()
                if (code.isEmpty()) {
                    statusText.text = "请输入配对码"
                    statusText.setTextColor(Color.RED)
                    statusText.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                val port = portStr.toIntOrNull() ?: -1
                if (port <= 0) {
                    statusText.text = "请输入有效端口"
                    statusText.setTextColor(Color.RED)
                    statusText.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                isEnabled = false
                text = "配对中..."
                statusText.text = "正在配对..."
                statusText.setTextColor(PINK)
                statusText.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.IO).launch {
                    val success = try {
                        val key = AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
                        val client = AdbPairingClient("127.0.0.1", port, code, key)
                        client.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(context, "配对成功！", Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            statusText.text = "配对失败，请检查配对码和端口"
                            statusText.setTextColor(Color.RED)
                            isEnabled = true
                            text = "确认配对"
                        }
                    }
                }
            }
        }.also { btnRow.addView(it, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)) }
        layout.addView(btnRow)

        card.addView(layout)
        return card
    }
}
