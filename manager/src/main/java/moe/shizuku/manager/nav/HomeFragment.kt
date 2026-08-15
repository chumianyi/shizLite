package moe.shizuku.manager.nav

import android.content.Intent
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
import rikka.shizuku.Shizuku

class HomeFragment : Fragment() {

    private lateinit var statusText: TextView
    private lateinit var originalStatusText: TextView
    private lateinit var wirelessCard: CardView
    private lateinit var rootCard: CardView
    private lateinit var wirelessBtn: Button
    private lateinit var rootBtn: Button
    private val ORIGINAL_PKG = "moe.shizuku.privileged.api"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val scroll = android.widget.ScrollView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
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
        wirelessBtn = Button(ctx).apply {
            text = "开始无线调试激活"
            setOnClickListener { startWirelessAdb() }
        }.also { wirelessLayout.addView(it) }
        wirelessCard.addView(wirelessLayout)
        view.addView(wirelessCard)

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
        rootBtn = Button(ctx).apply {
            text = "通过 Root 启动"
            setOnClickListener { startRoot() }
        }.also { rootLayout.addView(it) }
        rootCard.addView(rootLayout)
        view.addView(rootCard)

        return scroll
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val shizLiteActive = try { Shizuku.pingBinder() } catch (e: Throwable) { false }
        val originalInstalled = isPackageInstalled(ORIGINAL_PKG)
        val originalActive = originalInstalled && isOriginalActive()

        when {
            shizLiteActive -> {
                statusText.text = "✓ shizLite 服务已激活"
                statusText.setTextColor(0xFF4A9EFF.toInt())
            }
            originalActive -> {
                statusText.text = "⚠ 原版 Shizuku 已激活"
                statusText.setTextColor(0xFFFF6B6B.toInt())
            }
            else -> {
                statusText.text = "未激活，请选择下方方式激活"
                statusText.setTextColor(0xFF999999.toInt())
            }
        }

        originalStatusText.text = if (originalInstalled) "检测到原版 Shizuku${if (originalActive) "且已激活" else ""}" else ""
        originalStatusText.visibility = if (originalInstalled) View.VISIBLE else View.GONE

        val disabled = originalActive && !shizLiteActive
        wirelessBtn.isEnabled = !disabled
        rootBtn.isEnabled = !disabled
        wirelessCard.alpha = if (disabled) 0.5f else 1.0f
        rootCard.alpha = if (disabled) 0.5f else 1.0f
        wirelessBtn.text = if (disabled) "原版已激活，请勿重复激活" else "开始无线调试激活"
        rootBtn.text = if (disabled) "原版已激活，请勿重复激活" else "通过 Root 启动"
    }

    private fun startWirelessAdb() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                AdbDialogFragment().show(parentFragmentManager, "adb_pair")
            } else {
                startActivity(Intent(requireContext(), AdbPairingTutorialActivity::class.java))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("错误")
                .setMessage("启动无线调试失败：${e.message}")
                .setPositiveButton(android.R.string.ok, null)
                .show()
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
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("错误")
                .setMessage("Root 启动失败：${e.message}")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
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
