package moe.shizuku.manager.nav

import android.content.pm.PackageInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
import com.google.android.material.tabs.TabLayout
import moe.shizuku.manager.authorization.AuthorizationManager
import rikka.shizuku.Shizuku

class PermissionFragment : Fragment() {

    private lateinit var appListContainer: LinearLayout
    private lateinit var terminalListContainer: LinearLayout
    private lateinit var statusText: TextView
    private val PINK = Color.parseColor("#FF69B4")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val scroll = android.widget.ScrollView(ctx).apply {
            setBackgroundColor(Color.parseColor("#FFF0F6"))
        }
        val view = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        scroll.addView(view)

        TextView(ctx).apply {
            text = "授权管理"
            textSize = 20f
            setPadding(0, 0, 0, 8)
        }.also { view.addView(it) }

        statusText = TextView(ctx).apply {
            textSize = 13f
            setPadding(0, 0, 0, 24)
        }.also { view.addView(it) }

        // Tab 切换
        val tabLayout = TabLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
            addTab(newTab().setText("应用授权"))
            addTab(newTab().setText("终端授权"))
            setSelectedTabIndicatorColor(PINK)
            setTabTextColors(Color.parseColor("#888888"), PINK)
        }
        view.addView(tabLayout)

        appListContainer = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        terminalListContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        view.addView(appListContainer)
        view.addView(terminalListContainer)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    appListContainer.visibility = View.VISIBLE
                    terminalListContainer.visibility = View.GONE
                } else {
                    appListContainer.visibility = View.GONE
                    terminalListContainer.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // 终端授权说明
        TextView(ctx).apply {
            text = "终端授权"
            textSize = 16f
            setPadding(0, 0, 0, 8)
        }.also { terminalListContainer.addView(it) }
        TextView(ctx).apply {
            text = "支持终端应用获取 shizLite 权限执行 shell 命令。在终端应用中请求权限后，会在此显示并可撤销。"
            textSize = 13f
            setPadding(0, 0, 0, 24)
        }.also { terminalListContainer.addView(it) }
        TextView(ctx).apply {
            text = "暂无终端授权记录"
            textSize = 14f
            setTextColor(Color.parseColor("#999999"))
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 48)
        }.also { terminalListContainer.addView(it) }

        loadAppPermissions()
        return scroll
    }

    override fun onResume() {
        super.onResume()
        loadAppPermissions()
    }

    private fun loadAppPermissions() {
        val ctx = requireContext()
        appListContainer.removeAllViews()

        val shizLiteActive = try { Shizuku.pingBinder() } catch (e: Throwable) { false }
        if (!shizLiteActive) {
            statusText.text = "shizLite 未激活，无法管理授权"
            statusText.setTextColor(Color.parseColor("#FF6B6B"))
            TextView(ctx).apply {
                text = "请先在首页激活 shizLite 服务"
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 48, 0, 48)
                setTextColor(Color.parseColor("#999999"))
            }.also { appListContainer.addView(it) }
            return
        }

        statusText.text = "shizLite 已激活，可管理应用授权"
        statusText.setTextColor(PINK)

        val packages = try {
            AuthorizationManager.getPackages()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList<PackageInfo>()
        }

        if (packages.isEmpty()) {
            TextView(ctx).apply {
                text = "暂无应用请求 shizLite 权限"
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 48, 0, 48)
                setTextColor(Color.parseColor("#999999"))
            }.also { appListContainer.addView(it) }
            return
        }

        for (pi in packages) {
            val appName = try {
                ctx.packageManager.getApplicationLabel(pi.applicationInfo!!).toString()
            } catch (e: Exception) { pi.packageName }
            val granted = try {
                AuthorizationManager.granted(pi.packageName, pi.applicationInfo!!.uid)
            } catch (e: Exception) { false }

            val card = CardView(ctx).apply {
                radius = 16f
                cardElevation = 2f
                setContentPadding(32, 24, 32, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }
            }
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val textCol = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            TextView(ctx).apply {
                text = appName
                textSize = 15f
            }.also { textCol.addView(it) }
            TextView(ctx).apply {
                text = pi.packageName
                textSize = 11f
                setTextColor(Color.parseColor("#888888"))
            }.also { textCol.addView(it) }
            row.addView(textCol)

            val statusBtn = Button(ctx).apply {
                text = if (granted) "已授权" else "已拒绝"
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 100f
                    setColor(if (granted) PINK else Color.parseColor("#CCCCCC"))
                }
                setPadding(24, 12, 24, 12)
                setOnClickListener {
                    MaterialAlertDialogBuilder(ctx)
                        .setTitle(appName)
                        .setMessage("是否${if (granted) "撤销" else "授予"}该应用的 shizLite 权限？")
                        .setPositiveButton("确定") { _, _ ->
                            try {
                                if (granted) {
                                    AuthorizationManager.revoke(pi.packageName, pi.applicationInfo!!.uid)
                                } else {
                                    AuthorizationManager.grant(pi.packageName, pi.applicationInfo!!.uid)
                                }
                                loadAppPermissions()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        .setNegativeButton("取消", null)
                        .show()
                }
            }
            row.addView(statusBtn)
            card.addView(row)
            appListContainer.addView(card)
        }
    }
}
