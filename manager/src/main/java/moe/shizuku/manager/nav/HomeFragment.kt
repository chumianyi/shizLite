package moe.shizuku.manager.nav

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import moe.shizuku.manager.R
import moe.shizuku.manager.home.HomeActivity
import moe.shizuku.manager.module.ModuleActivity
import rikka.shizuku.Shizuku

class HomeFragment : Fragment() {

    private lateinit var statusText: TextView
    private lateinit var originalStatusText: TextView
    private lateinit var activateBtn: Button
    private lateinit var openOriginalBtn: Button
    private val ORIGINAL_PKG = "moe.shizuku.privileged.api"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        statusText = TextView(requireContext()).apply {
            textSize = 18f
            setPadding(0, 0, 0, 24)
        }
        originalStatusText = TextView(requireContext()).apply {
            textSize = 14f
            setPadding(0, 0, 0, 24)
        }
        activateBtn = Button(requireContext()).apply {
            text = "进入激活页面"
            setOnClickListener {
                startActivity(Intent(requireContext(), HomeActivity::class.java))
            }
        }
        openOriginalBtn = Button(requireContext()).apply {
            text = "打开原版 Shizuku"
            visibility = View.GONE
            setOnClickListener { showWarningAndOpenOriginal() }
        }

        view.addView(statusText)
        view.addView(originalStatusText)
        view.addView(activateBtn)
        view.addView(openOriginalBtn)
        return view
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
                statusText.text = "当前服务：shizLite（已激活）"
                statusText.setTextColor(0xFF4A9EFF.toInt())
            }
            originalActive -> {
                statusText.text = "当前服务：原版 Shizuku（已激活）"
                statusText.setTextColor(0xFFFF6B6B.toInt())
            }
            else -> {
                statusText.text = "未激活，请选择一种方式激活"
                statusText.setTextColor(0xFF999999.toInt())
            }
        }

        if (originalInstalled) {
            originalStatusText.text = "检测到原版 Shizuku 已安装${if (originalActive) "且已激活" else "（未激活）"}"
            originalStatusText.visibility = View.VISIBLE
            openOriginalBtn.visibility = View.VISIBLE
        } else {
            originalStatusText.visibility = View.GONE
            openOriginalBtn.visibility = View.GONE
        }

        // 互斥：原版已激活时，禁用 shizLite 激活
        if (originalActive && !shizLiteActive) {
            activateBtn.isEnabled = false
            activateBtn.alpha = 0.5f
            activateBtn.text = "原版 Shizuku 已激活，请勿重复激活"
        } else {
            activateBtn.isEnabled = true
            activateBtn.alpha = 1.0f
            activateBtn.text = "进入激活页面"
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

    private fun isOriginalActive(): Boolean {
        // 检查原版 Shizuku 服务是否运行
        return try {
            val am = requireContext().getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            @Suppress("DEPRECATION")
            am.getRunningServices(100).any { it.service.packageName == ORIGINAL_PKG }
        } catch (e: Exception) {
            false
        }
    }

    private fun showWarningAndOpenOriginal() {
        val shizLiteActive = try { Shizuku.pingBinder() } catch (e: Throwable) { false }
        if (shizLiteActive) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ 警告")
                .setMessage("千万不要再次激活，否则可能会导致设备损坏！")
                .setPositiveButton("我知道了", null)
                .setNegativeButton("仍然打开") { _, _ -> openOriginal() }
                .show()
        } else {
            openOriginal()
        }
    }

    private fun openOriginal() {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(ORIGINAL_PKG)
            if (intent != null) startActivity(intent)
        } catch (e: Exception) {
        }
    }
}
