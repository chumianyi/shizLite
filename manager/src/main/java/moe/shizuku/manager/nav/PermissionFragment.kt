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
import moe.shizuku.manager.R
import moe.shizuku.manager.management.ApplicationManagementActivity

class PermissionFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        TextView(requireContext()).apply {
            text = "授权管理"
            textSize = 20f
            setPadding(0, 0, 0, 16)
        }.also { view.addView(it) }

        TextView(requireContext()).apply {
            text = "管理已请求 shizLite 权限的应用和终端授权"
            textSize = 14f
            setPadding(0, 0, 0, 32)
        }.also { view.addView(it) }

        Button(requireContext()).apply {
            text = "应用授权管理"
            setOnClickListener {
                startActivity(Intent(requireContext(), ApplicationManagementActivity::class.java))
            }
        }.also { view.addView(it) }

        return view
    }
}
