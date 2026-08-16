package moe.shizuku.manager.nav

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.shizuku.manager.R
import moe.shizuku.manager.adb.AdbPairingTutorialActivity
import moe.shizuku.manager.home.AdbDialogFragment
import moe.shizuku.manager.starter.StarterActivity
import moe.shizuku.manager.utils.EnvironmentUtils
import rikka.shizuku.Shizuku

class HomeFragment : Fragment() {

    private lateinit var statusText: TextView
    private lateinit var originalStatusText: TextView
    private lateinit var wirelessCard: CardView
    private lateinit var rootCard: CardView
    private lateinit var pairBtn: Button
    private lateinit var activateBtn: Button
    private lateinit var rootBtn: Button
    private val ORIGINAL_PKG = "moe.shizuku.privileged.api"
    private val PINK = Color.parseColor("#FF69B4")
    private val PINK_DARK = Color.parseColor("#E91E63")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val scroll = android.widget.ScrollView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#FFF0F6"))
        }
        val view = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        scroll.addView(view)

        statusText = TextView(ctx).apply {
            textSize = 18f
            setPadding(0, 0, 0, 16)
            gravity = Gravity.CENTER
        }
        originalStatusText = TextView(ctx).apply {
            textSize = 13f
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
        }
        view.addView(statusText)
        view.addView(originalStatusText)

        // 无线调试卡片
        wirelessCard = CardView(ctx).apply {
            radius = 24f
            cardElevation = 4f
            setContentPadding(48, 48, 48, 48)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
        val wirelessLayout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        TextView(ctx).apply {
            text = "无线调试激活"
            textSize = 16f
            setPadding(0, 0, 0, 8)
        }.also { wirelessLayout.addView(it) }
        TextView(ctx).apply {
            text = "Android 11+ 通过系统无线调试配对激活，无需电脑"
            textSize = 13f
            setPadding(0, 0, 0, 24)
        }.also { wirelessLayout.addView(it) }
        // 双按钮并排
        val btnRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        pairBtn = createPinkButton(ctx, "配对").apply {
            setOnClickListener { startPairing() }
        }
        activateBtn = createPinkButton(ctx, "激活").apply {
            setOnClickListener { startWirelessActivate() }
        }
        btnRow.addView(pairBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 16 })
        btnRow.addView(activateBtn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        wirelessLayout.addView(btnRow)
        wirelessCard.addView(wirelessLayout)
        view.addView(wirelessCard)

        // Root 卡片
        rootCard = CardView(ctx).apply {
            radius = 24f
            cardElevation = 4f
            setContentPadding(48, 48, 48, 48)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
        val rootLayout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        TextView(ctx).apply {
            text = "Root 激活"
            textSize = 16f
            setPadding(0, 0, 0, 8)
        }.also { rootLayout.addView(it) }
        TextView(ctx).apply {
            text = "通过 Root 权限直接启动 shizLite 服务，支持开机自启"
            textSize = 13f
            setPadding(0, 0, 0, 24)
        }.also { rootLayout.addView(it) }
        rootBtn = createPinkButton(ctx, "通过 Root 启动").apply {
            setOnClickListener { startRoot() }
        }
        rootLayout.addView(rootBtn)
        rootCard.addView(rootLayout)
        view.addView(rootCard)

        return scroll
    }

    private fun createPinkButton(ctx: android.content.Context, text: String): Button {
        return Button(ctx).apply {
            this.text = text
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(PINK)
            }
            setPadding(32, 24, 32, 24)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val shizLiteActive = try { Shizuku.pingBinder() } catch (e: Throwable) { false }
        val originalInstalled = isPackageInstalled(ORIGINAL_PKG)
        val originalActive = originalInstalled && isOriginalActive()
        val adbPort = try { EnvironmentUtils.getAdbTcpPort() } catch (e: Throwable) { -1 }
        val paired = adbPort > 0

        // 前台服务：激活时启动常驻通知，未激活时停止
        val ctx = requireContext()
        val fgIntent = android.content.Intent(ctx, moe.shizuku.manager.ShizLiteForegroundService::class.java)
        if (shizLiteActive) {
            try { ctx.startForegroundService(fgIntent) } catch (e: Exception) { e.printStackTrace() }
        } else {
            try { ctx.stopService(fgIntent) } catch (e: Exception) { e.printStackTrace() }
        }

        when {
            shizLiteActive -> {
                statusText.text = "✓ shizLite 服务已激活"
                statusText.setTextColor(PINK)
            }
            originalActive -> {
                statusText.text = "⚠ 原版 Shizuku 已激活"
                statusText.setTextColor(Color.parseColor("#FF6B6B"))
            }
            else -> {
                statusText.text = "未激活，请选择下方方式激活"
                statusText.setTextColor(Color.parseColor("#999999"))
            }
        }

        originalStatusText.text = if (originalInstalled) "检测到原版 Shizuku${if (originalActive) "且已激活" else ""}" else ""
        originalStatusText.visibility = if (originalInstalled) View.VISIBLE else View.GONE

        val disabled = originalActive && !shizLiteActive

        // 无线调试双按钮状态
        if (shizLiteActive) {
            pairBtn.text = "已配对"
            pairBtn.isEnabled = false
            activateBtn.text = "已激活"
            activateBtn.isEnabled = false
        } else if (disabled) {
            pairBtn.text = "原版已激活"
            pairBtn.isEnabled = false
            activateBtn.text = "请勿重复"
            activateBtn.isEnabled = false
        } else {
            pairBtn.text = if (paired) "重新配对" else "配对"
            pairBtn.isEnabled = true
            activateBtn.text = "激活"
            activateBtn.isEnabled = paired
        }

        // Root 按钮状态
        rootBtn.isEnabled = !disabled && !shizLiteActive
        rootBtn.text = when {
            shizLiteActive -> "已激活"
            disabled -> "原版已激活，请勿重复"
            else -> "通过 Root 启动"
        }

        wirelessCard.alpha = if (disabled && !shizLiteActive) 0.5f else 1.0f
        rootCard.alpha = if (disabled && !shizLiteActive) 0.5f else 1.0f
    }

    private fun startPairing() {
        try {
            startActivity(android.content.Intent(requireContext(), moe.shizuku.manager.adb.PairingTutorialActivity::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
            showError("打开教程失败：${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startPairing()
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("需要通知权限")
                    .setMessage("无线调试配对需要通知权限才能接收配对码通知，请在设置中授予通知权限后重试。")
                    .setPositiveButton("去设置") { _, _ ->
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                            }
                            startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }

    private fun startWirelessActivate() {
        try {
            val port = EnvironmentUtils.getAdbTcpPort()
            if (port <= 0) {
                showError("未检测到无线调试端口，请先完成配对")
                return
            }
            val intent = Intent(requireContext(), StarterActivity::class.java).apply {
                putExtra(StarterActivity.EXTRA_IS_ROOT, false)
                putExtra(StarterActivity.EXTRA_HOST, "127.0.0.1")
                putExtra(StarterActivity.EXTRA_PORT, port)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showError("激活失败：${e.message}")
        }
    }

    private fun startRoot() {
        try {
            val intent = Intent(requireContext(), StarterActivity::class.java).apply {
                putExtra(StarterActivity.EXTRA_IS_ROOT, true)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Root 启动失败：${e.message}")
        }
    }

    private fun showError(msg: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("错误")
            .setMessage(msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun isPackageInstalled(pkg: String): Boolean {
        return try {
            requireContext().packageManager.getPackageInfo(pkg, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun isOriginalActive(): Boolean {
        return try {
            val am = requireContext().getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            am.getRunningServices(100).any { it.service.packageName == ORIGINAL_PKG }
        } catch (e: Exception) {
            false
        }
    }
}
