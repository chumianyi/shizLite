package com.chumian.shizlite.ui.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chumian.shizlite.module.Module
import com.chumian.shizlite.module.ModuleManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(navController: NavController) {
    var modules by remember { mutableStateOf(ModuleManager.getInstalledModules()) }
    var selectedModule by remember { mutableStateOf<Module?>(null) }
    val scope = rememberCoroutineScope()
    var runOutput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        modules = ModuleManager.getInstalledModules()
    }

    if (selectedModule != null) {
        ModuleDetailScreen(
            module = selectedModule!!,
            onBack = { selectedModule = null },
            onActivate = {
                ModuleManager.activateModule(it.id)
                modules = ModuleManager.getInstalledModules()
                selectedModule = ModuleManager.getInstalledModules().firstOrNull { m -> m.id == it.id }
            },
            onDeactivate = {
                ModuleManager.deactivateModule(it.id)
                modules = ModuleManager.getInstalledModules()
                selectedModule = ModuleManager.getInstalledModules().firstOrNull { m -> m.id == it.id }
            },
            onUninstall = {
                ModuleManager.uninstallModule(it.id)
                modules = ModuleManager.getInstalledModules()
                selectedModule = null
            },
            onRun = { action ->
                scope.launch {
                    val result = ModuleManager.runModule(selectedModule!!, action)
                    runOutput = "退出码: ${result.exitCode}\nstdout:\n${result.stdout}\nstderr:\n${result.stderr}"
                }
            },
            runOutput = runOutput
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("已安装模块", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (modules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无已安装模块，请在设置中导入模块", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(modules) { module ->
                        ModuleCard(module = module) {
                            selectedModule = module
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleCard(module: Module, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (module.requiredLevel == "root") Icons.Default.Security
                else Icons.Default.Extension,
                contentDescription = null,
                tint = if (module.requiredLevel == "root") MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(module.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("v${module.version} · ${module.author}", style = MaterialTheme.typography.bodySmall)
                Text(module.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            Column(horizontalAlignment = Alignment.End) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (module.requiredLevel == "root") "Root" else "普通") },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (module.requiredLevel == "root") MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (module.activated) "已激活" else "未激活",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (module.activated) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
