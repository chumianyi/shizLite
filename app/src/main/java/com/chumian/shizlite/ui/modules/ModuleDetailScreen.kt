package com.chumian.shizlite.ui.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chumian.shizlite.module.Module

@Composable
fun ModuleDetailScreen(
    module: Module,
    onBack: () -> Unit,
    onActivate: (Module) -> Unit,
    onDeactivate: (Module) -> Unit,
    onUninstall: (Module) -> Unit,
    onRun: (String) -> Unit,
    runOutput: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(module.name) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("模块信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("模块ID", module.id)
                    InfoRow("版本", module.version)
                    InfoRow("作者", module.author)
                    InfoRow("权限级别", if (module.requiredLevel == "root") "Root" else "Shizuku/普通")
                    InfoRow("入口脚本", module.entryScript)
                    InfoRow("状态", if (module.activated) "已激活" else "未激活")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("模块描述", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(module.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (!module.activated) {
                Button(
                    onClick = { onActivate(module) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("激活模块") }
            } else {
                OutlinedButton(
                    onClick = { onDeactivate(module) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("停用模块") }
            }

            if (module.activated) {
                Text("模块操作", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (module.id == "system_optimizer") {
                    ModuleActionButton("清理缓存") { onRun("clean_cache") }
                    ModuleActionButton("禁用动画(0x)") { onRun("disable_anim") }
                    ModuleActionButton("恢复动画(1x)") { onRun("enable_anim") }
                    ModuleActionButton("查看系统属性") { onRun("system_props") }
                    ModuleActionButton("列出已冻结应用") { onRun("list_frozen") }
                } else if (module.id == "root_optimizer") {
                    ModuleActionButton("深度清理缓存") { onRun("deep_clean") }
                    ModuleActionButton("内核优化") { onRun("kernel_tune") }
                    ModuleActionButton("修改hosts") { onRun("modify_hosts") }
                    ModuleActionButton("CPU性能模式") { onRun("cpu_performance") }
                    ModuleActionButton("CPU省电模式") { onRun("cpu_powersave") }
                    ModuleActionButton("查看SELinux状态") { onRun("selinux_status") }
                    ModuleActionButton("设置分辨率1080p") { onRun("set_resolution") }
                    ModuleActionButton("完整pm命令集") { onRun("pm_full") }
                } else {
                    ModuleActionButton("运行模块") { onRun("main") }
                }
            }

            if (runOutput.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        runOutput,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            TextButton(
                onClick = { onUninstall(module) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("卸载模块", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ModuleActionButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}
