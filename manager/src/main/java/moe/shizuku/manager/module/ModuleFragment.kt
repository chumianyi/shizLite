package moe.shizuku.manager.module

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.shizuku.manager.R

class ModuleFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ModuleActivity.ModuleAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext()
        if (ModuleManager.getInstalledModules().isEmpty()) {
            ModuleManager.installPrebuiltModules(context)
        }
        val view = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            setPadding(32, 32, 32, 32)
            clipToPadding = false
        }
        recyclerView = view
        adapter = ModuleActivity.ModuleAdapter(ModuleManager.getInstalledModules().toMutableList())
        adapter.onItemClick = { module -> showModuleDetail(module) }
        recyclerView.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.modules = ModuleManager.getInstalledModules().toMutableList()
            adapter.notifyDataSetChanged()
        }
    }

    private fun showModuleDetail(module: Module) {
        val items = arrayOf(
            if (module.activated) "停用模块" else "激活模块",
            "运行模块",
            "查看详情",
            "卸载模块"
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(module.name)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        if (module.activated) ModuleManager.deactivateModule(module.id)
                        else ModuleManager.activateModule(module.id)
                        adapter.modules = ModuleManager.getInstalledModules().toMutableList()
                        adapter.notifyDataSetChanged()
                    }
                    1 -> {
                        if (!module.activated) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setMessage("请先激活模块")
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                            return@setItems
                        }
                        val result = ModuleManager.runModule(module)
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("运行结果 (exit: ${result.first})")
                            .setMessage(result.second.take(2000))
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    2 -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(module.name)
                            .setMessage("版本: ${module.version}\n作者: ${module.author}\n权限: ${module.requiredLevel}\n状态: ${if (module.activated) "已激活" else "未激活"}\n\n${module.description}")
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    3 -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("确认卸载")
                            .setMessage("确定卸载模块 \"${module.name}\" 吗？")
                            .setPositiveButton("卸载") { _, _ ->
                                ModuleManager.uninstallModule(module.id)
                                adapter.modules = ModuleManager.getInstalledModules().toMutableList()
                                adapter.notifyDataSetChanged()
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                    }
                }
            }
            .show()
    }
}
