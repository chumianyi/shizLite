package com.chumian.shizlite.ui.permissions

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
import com.chumian.shizlite.core.PermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("应用授权", "终端授权")
    var apps by remember { mutableStateOf(PermissionManager.getAuthorizedApps()) }
    var terminals by remember { mutableStateOf(PermissionManager.getAuthorizedTerminals()) }
    var showAddAppDialog by remember { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    LaunchedEffect(selectedTab) {
        apps = PermissionManager.getAuthorizedApps()
        terminals = PermissionManager.getAuthorizedTerminals()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (selectedTab == 0) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("已授权应用 (${apps.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        installedApps = PermissionManager.getInstalledApps()
                        showAddAppDialog = true
                    }) { Icon(Icons.Default.Add, contentDescription = "添加") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (apps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无已授权应用", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(apps) { app ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.appName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                                    }
                                    TextButton(onClick = {
                                        PermissionManager.revokeApp(app.packageName)
                                        apps = PermissionManager.getAuthorizedApps()
                                    }) { Text("撤销", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("已授权终端 (${terminals.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (terminals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无已授权终端", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(terminals) { term ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(term.terminalPackage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text("已授权", style = MaterialTheme.typography.bodySmall)
                                    }
                                    TextButton(onClick = {
                                        PermissionManager.revokeTerminal(term.terminalPackage)
                                        terminals = PermissionManager.getAuthorizedTerminals()
                                    }) { Text("撤销", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAppDialog) {
        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            title = { Text("选择应用授权") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    items(installedApps) { (pkg, name) ->
                        TextButton(
                            onClick = {
                                PermissionManager.grantApp(pkg)
                                apps = PermissionManager.getAuthorizedApps()
                                showAddAppDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                                Text(pkg, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddAppDialog = false }) { Text("取消") } }
        )
    }
}
