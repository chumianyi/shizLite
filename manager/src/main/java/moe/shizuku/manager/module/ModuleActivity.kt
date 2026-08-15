package moe.shizuku.manager.module

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.databinding.ActivityModuleBinding

class ModuleActivity : AppBarActivity() {

    private lateinit var binding: ActivityModuleBinding
    private lateinit var adapter: ModuleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.module_title)

        if (ModuleManager.getInstalledModules().isEmpty()) {
            ModuleManager.installPrebuiltModules(this)
        }

        adapter = ModuleAdapter(ModuleManager.getInstalledModules().toMutableList())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.onItemClick = { module ->
            showModuleDetail(module)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.modules = ModuleManager.getInstalledModules().toMutableList()
        adapter.notifyDataSetChanged()
    }

    private fun showModuleDetail(module: Module) {
        val items = arrayOf(
            if (module.activated) "停用模块" else "激活模块",
            "运行模块",
            "查看详情",
            "卸载模块"
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(module.name)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        if (module.activated) {
                            ModuleManager.deactivateModule(module.id)
                        } else {
                            ModuleManager.activateModule(module.id)
                        }
                        adapter.modules = ModuleManager.getInstalledModules().toMutableList()
                        adapter.notifyDataSetChanged()
                    }
                    1 -> {
                        if (!module.activated) {
                            MaterialAlertDialogBuilder(this)
                                .setMessage("请先激活模块")
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                            return@setItems
                        }
                        val result = ModuleManager.runModule(module)
                        MaterialAlertDialogBuilder(this)
                            .setTitle("运行结果 (exit: ${result.first})")
                            .setMessage(result.second.take(2000))
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    2 -> {
                        MaterialAlertDialogBuilder(this)
                            .setTitle(module.name)
                            .setMessage("版本: ${module.version}\n作者: ${module.author}\n权限: ${module.requiredLevel}\n状态: ${if (module.activated) "已激活" else "未激活"}\n\n${module.description}")
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    3 -> {
                        MaterialAlertDialogBuilder(this)
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

    class ModuleAdapter(var modules: MutableList<Module>) : RecyclerView.Adapter<ModuleAdapter.ViewHolder>() {
        var onItemClick: ((Module) -> Unit)? = null

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.module_name)
            val desc: TextView = view.findViewById(R.id.module_desc)
            val status: TextView = view.findViewById(R.id.module_status)
            val level: TextView = view.findViewById(R.id.module_level)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_module, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val module = modules[position]
            holder.name.text = module.name
            holder.desc.text = module.description
            holder.status.text = if (module.activated) "已激活" else "未激活"
            holder.level.text = if (module.requiredLevel == "root") "Root" else "普通"
            holder.itemView.setOnClickListener { onItemClick?.invoke(module) }
        }

        override fun getItemCount() = modules.size
    }
}
