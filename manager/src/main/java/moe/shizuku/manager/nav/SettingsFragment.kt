package moe.shizuku.manager.nav

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import moe.shizuku.manager.settings.SettingsFragment as OriginalSettingsFragment

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val view = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FFF0F6"))
        }

        TextView(ctx).apply {
            text = "设置"
            textSize = 20f
            setPadding(48, 48, 48, 16)
        }.also { view.addView(it) }

        val container = FrameLayout(ctx).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        view.addView(container)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(container.id, OriginalSettingsFragment())
                .commit()
        }

        return view
    }
}
