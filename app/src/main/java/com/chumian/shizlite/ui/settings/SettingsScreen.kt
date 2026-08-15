package com.chumian.shizlite.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chumian.shizlite.ShizLiteApp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var darkTheme by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("外观", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("深色模式")
                    Switch(checked = darkTheme, onCheckedChange = { darkTheme = it })
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("模块", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "*/*"
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导入模块 (.slm)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("模块格式：.slm (zip压缩包)，包含module.json元数据和可执行脚本", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), onClick = { showAbout = true }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("shizLite v1.0.0", style = MaterialTheme.typography.bodyMedium)
                Text("基于 Shizuku 原理开发的系统优化工具", style = MaterialTheme.typography.bodySmall)
                Text("GPL-3.0 开源协议", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("关于 shizLite") },
            text = {
                Column {
                    Text("shizLite v1.0.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("本应用基于 Shizuku (https://github.com/RikkaApps/Shizuku) 原理开发，遵循 GPL-3.0 协议。")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("本版本优化：", fontWeight = FontWeight.Bold)
                    Text("• 精简激活方式，仅保留无线调试与Root激活")
                    Text("• 新增模块系统，支持自定义.slm模块扩展")
                    Text("• Material 3 UI重构，支持深色/浅色主题")
                    Text("• 应用与终端双重授权管理")
                    Text("• 预置普通优化与Root高级优化双模块")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("包名: com.chumian.shizLite")
                    Text("作者: Chumian")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("关闭") }
            }
        )
    }
}
