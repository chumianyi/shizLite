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
import moe.shizuku.manager.settings.SettingsActivity

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        TextView(requireContext()).apply {
            text = "设置"
            textSize = 20f
            setPadding(0, 0, 0, 32)
        }.also { view.addView(it) }

        Button(requireContext()).apply {
            text = "打开设置页面"
            setOnClickListener {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            }
        }.also { view.addView(it) }

        return view
    }
}
