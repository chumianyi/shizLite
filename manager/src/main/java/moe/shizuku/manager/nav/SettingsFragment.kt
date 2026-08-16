package moe.shizuku.manager.nav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import moe.shizuku.manager.settings.SettingsFragment as OriginalSettingsFragment

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val container = FrameLayout(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(container.id, OriginalSettingsFragment())
                .commit()
        }
        return container
    }
}
