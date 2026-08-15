package com.chumian.shizlite.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chumian.shizlite.core.ActivationManager
import com.chumian.shizlite.core.ShellExecutor
import com.chumian.shizlite.module.ModuleManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var isActive by remember { mutableStateOf(ActivationManager.isActive) }
    var method by remember { mutableStateOf(ActivationManager.activationMethod) }
    var showActivateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (ModuleManager.getInstalledModules().isEmpty()) {
            ModuleManager.installPrebuiltModules()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isActive) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isActive) "已激活" else "未激活",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            when (method) {
                                ActivationManager.ActivationMethod.ROOT -> "Root 激活模式"
                                ActivationManager.ActivationMethod.WIRELESS_DEBUGGING -> "无线调试激活模式"
                                ActivationManager.ActivationMethod.NONE -> "请选择激活方式"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (!isActive) {
                    Button(
                        onClick = { showActivateDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("立即激活") }
                } else {
                    OutlinedButton(
                        onClick = {
                            ActivationManager.deactivate()
                            isActive = false
                            method = ActivationManager.activationMethod
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("停止服务") }
                }
            }
        }

        Text("快捷功能", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Icons.Default.CleaningServices, "清理缓存", "pm trim-caches") {
                scope.launch {
                    val r = ShellExecutor.execute("pm trim-caches 999999999999")
                    statusText = "退出码: ${r.exitCode}\n${r.stdout.take(200)}"
                }
            }
            QuickActionCard(Icons.Default.Speed, "动画缩放", "0.5x") {
                scope.launch {
                    ShellExecutor.setAnimationScale(0.5f)
                    statusText = "动画缩放已设为 0.5x"
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(Icons.Default.Info, "系统属性", "getprop") {
                scope.launch {
                    val r = ShellExecutor.execute("getprop ro.build.version.release")
                    statusText = "Android版本: ${r.stdout.trim()}"
                }
            }
            QuickActionCard(Icons.Default.Extension, "模块管理", "查看模块") {
                navController.navigate("modules")
            }
        }

        if (statusText.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    statusText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showActivateDialog) {
        AlertDialog(
            onDismissRequest = { showActivateDialog = false },
            title = { Text("选择激活方式") },
            text = {
                Column {
                    Text("shizLite 仅支持以下两种激活方式：", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val ok = ActivationManager.activateViaWirelessDebugging()
                            isActive = ok
                            method = ActivationManager.activationMethod
                            showActivateDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("无线调试激活") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val ok = ActivationManager.activateViaRoot()
                            isActive = ok
                            method = ActivationManager.activationMethod
                            showActivateDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Root 激活") }
                }
            }
        )
    }
}

@Composable
fun QuickActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.weight(1f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
