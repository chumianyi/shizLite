package moe.shizuku.manager.adb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.ShizukuSettings

class FloatingPairingWindow(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "shizlite_pairing"
        private const val NOTIFICATION_ID = 3001
        private const val ACTION_TOGGLE = "com.chumian.shizlite.TOGGLE_FLOATING"
        private var instance: FloatingPairingWindow? = null

        fun sendPairingNotification(context: Context) {
            ensureChannel(context)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val toggleIntent = Intent(context, FloatingPairingReceiver::class.java).apply {
                action = ACTION_TOGGLE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("shizLite 配对工具")
                .setContentText("点击开启配对悬浮窗，可在任意界面输入配对码")
                .setSmallIcon(android.R.drawable.ic_menu_add)
                .setColor(0xFFFF69B4.toInt())
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(android.R.drawable.ic_menu_add, "开启悬浮窗", pendingIntent)
                .setContentIntent(pendingIntent)
                .build()
            nm.notify(NOTIFICATION_ID, notification)
        }

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID, "配对工具", NotificationManager.IMPORTANCE_LOW
                    ).apply { description = "无线调试配对悬浮窗控制" }
                    nm.createNotificationChannel(channel)
                }
            }
        }

        fun toggle(context: Context) {
            if (instance?.isShowing() == true) {
                instance?.dismiss()
            } else {
                instance = FloatingPairingWindow(context.applicationContext)
                instance?.show()
            }
        }
    }

    class FloatingPairingReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == ACTION_TOGGLE) {
                toggle(context)
            }
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val PINK = Color.parseColor("#FF69B4")
    private val PINK_DARK = Color.parseColor("#E91E63")

    fun isShowing(): Boolean = floatingView != null

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
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 200
        }

        val card = createPairingCard()
        floatingView = card
        windowManager?.addView(card, layoutParams)
        updateNotification(true)

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
        updateNotification(false)
    }

    private fun updateNotification(showing: Boolean) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val toggleIntent = Intent(context, FloatingPairingReceiver::class.java).apply {
            action = ACTION_TOGGLE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("shizLite 配对工具")
            .setContentText(if (showing) "配对悬浮窗已开启，点击关闭" else "点击开启配对悬浮窗")
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setColor(0xFFFF69B4.toInt())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                if (showing) "关闭悬浮窗" else "开启悬浮窗",
                pendingIntent
            )
            .setContentIntent(pendingIntent)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createPairingCard(): View {
        val card = CardView(context).apply {
            radius = 20f
            cardElevation = 12f
            setContentPadding(36, 32, 36, 32)
            setCardBackgroundColor(Color.WHITE)
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            minimumWidth = 620
        }

        // 标题行
        val titleRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 16)
        }
        TextView(context).apply {
            text = "无线调试配对"
            textSize = 17f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }.also { titleRow.addView(it) }
        // 关闭按钮 X
        TextView(context).apply {
            text = "✕"
            textSize = 16f
            setTextColor(Color.parseColor("#999999"))
            setPadding(16, 4, 4, 4)
            setOnClickListener { dismiss() }
        }.also { titleRow.addView(it) }
        layout.addView(titleRow)

        // 配对码
        TextView(context).apply {
            text = "配对码"
            textSize = 12f
            setPadding(0, 0, 0, 6)
            setTextColor(Color.parseColor("#888888"))
        }.also { layout.addView(it) }
        val codeInput = EditText(context).apply {
            hint = "6 位数字"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            maxLines = 1
            setPadding(24, 18, 24, 18)
            setTextColor(Color.BLACK)
            setHintTextColor(Color.parseColor("#CCCCCC"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#FFF5FA"))
                setStroke(2, Color.parseColor("#FFD6E8"))
            }
        }.also { layout.addView(it) }

        // 端口
        TextView(context).apply {
            text = "端口"
            textSize = 12f
            setPadding(0, 16, 0, 6)
            setTextColor(Color.parseColor("#888888"))
        }.also { layout.addView(it) }
        val portInput = EditText(context).apply {
            hint = "端口号"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            maxLines = 1
            setPadding(24, 18, 24, 18)
            setTextColor(Color.BLACK)
            setHintTextColor(Color.parseColor("#CCCCCC"))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#FFF5FA"))
                setStroke(2, Color.parseColor("#FFD6E8"))
            }
        }.also { layout.addView(it) }

        val statusText = TextView(context).apply {
            textSize = 12f
            setPadding(0, 12, 0, 0)
            visibility = View.GONE
        }.also { layout.addView(it) }

        // 确认按钮
        Button(context).apply {
            text = "确认配对"
            setTextColor(Color.WHITE)
            textSize = 14f
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(PINK)
            }
            setPadding(0, 20, 0, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 20 }
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
                            Toast.makeText(context, "✓ 配对成功！可以激活了", Toast.LENGTH_LONG).show()
                            codeInput.text.clear()
                            portInput.text.clear()
                            statusText.visibility = View.GONE
                        } else {
                            statusText.text = "配对失败，请检查配对码和端口"
                            statusText.setTextColor(Color.RED)
                        }
                        isEnabled = true
                        text = "确认配对"
                    }
                }
            }
        }.also { layout.addView(it) }

        card.addView(layout)
        return card
    }
}
