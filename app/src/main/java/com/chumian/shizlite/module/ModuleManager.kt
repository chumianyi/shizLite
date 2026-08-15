package com.chumian.shizlite.module

import android.content.Context
import android.content.SharedPreferences
import com.chumian.shizlite.ShizLiteApp
import com.chumian.shizlite.core.ActivationManager
import com.chumian.shizlite.core.ShellExecutor
import com.chumian.shizlite.core.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

object ModuleManager {

    private const val PREFS_NAME = "shizlite_modules"
    private const val KEY_MODULES = "installed_modules"

    private val prefs: SharedPreferences =
        ShizLiteApp.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val modulesDir: File
        get() = File(ShizLiteApp.appContext.filesDir, "modules").apply { mkdirs() }

    fun getInstalledModules(): List<Module> {
        val json = prefs.getString(KEY_MODULES, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<Module>()
        for (i in 0 until array.length()) {
            list.add(Module.fromJson(array.getJSONObject(i)))
        }
        return list
    }

    private fun saveModules(modules: List<Module>) {
        val array = JSONArray()
        modules.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_MODULES, array.toString()).apply()
    }

    suspend fun installModule(slmFile: File): Result<Module> = withContext(Dispatchers.IO) {
        try {
            val zipFile = ZipFile(slmFile)
            val entry = zipFile.getEntry("module.json")
                ?: return@withContext Result.failure(Exception("module.json not found"))
            val json = JSONObject(zipFile.getInputStream(entry).bufferedReader().use { it.readText() })
            val module = Module.fromJson(json)
            val targetDir = File(modulesDir, module.id).apply { mkdirs() }
            zipFile.entries().toList().forEach { zipEntry ->
                if (!zipEntry.isDirectory) {
                    val outFile = File(targetDir, zipEntry.name)
                    outFile.parentFile?.mkdirs()
                    zipFile.getInputStream(zipEntry).use { input ->
                        FileOutputStream(outFile).use { output -> input.copyTo(output) }
                    }
                }
            }
            zipFile.close()
            val installed = module.copy(installed = true, activated = false, installPath = targetDir.absolutePath)
            val modules = getInstalledModules().toMutableList()
            modules.removeAll { it.id == module.id }
            modules.add(installed)
            saveModules(modules)
            Result.success(installed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun uninstallModule(moduleId: String) {
        val modules = getInstalledModules().filter { it.id != moduleId }
        saveModules(modules)
        File(modulesDir, moduleId).deleteRecursively()
    }

    fun activateModule(moduleId: String): Boolean {
        val modules = getInstalledModules().toMutableList()
        val index = modules.indexOfFirst { it.id == moduleId }
        if (index >= 0) {
            modules[index] = modules[index].copy(activated = true)
            saveModules(modules)
            return true
        }
        return false
    }

    fun deactivateModule(moduleId: String) {
        val modules = getInstalledModules().toMutableList()
        val index = modules.indexOfFirst { it.id == moduleId }
        if (index >= 0) {
            modules[index] = modules[index].copy(activated = false)
            saveModules(modules)
        }
    }

    fun isModuleActivated(moduleId: String): Boolean {
        return getInstalledModules().any { it.id == moduleId && it.activated }
    }

    suspend fun runModule(module: Module, action: String = "main"): ShellResult = withContext(Dispatchers.IO) {
        if (!module.activated) {
            return@withContext ShellResult(-1, "", "Module not activated")
        }
        val level = if (module.requiredLevel == "root") {
            if (ActivationManager.activationMethod == ActivationManager.ActivationMethod.ROOT) {
                ShellExecutor.PrivilegeLevel.ROOT
            } else {
                return@withContext ShellResult(-1, "", "Root activation required for this module")
            }
        } else {
            ActivationManager.getPrivilegeLevel()
        }
        val scriptFile = File(module.installPath, module.entryScript)
        if (!scriptFile.exists()) {
            return@withContext ShellResult(-1, "", "Entry script not found")
        }
        ShellExecutor.execute("sh ${scriptFile.absolutePath} $action", level)
    }

    suspend fun installPrebuiltModules() {
        val assets = ShizLiteApp.appContext.assets
        listOf("module_optimizer.slm", "module_root_optimizer.slm").forEach { name ->
            try {
                val tmpFile = File(ShizLiteApp.appContext.cacheDir, name)
                ShizLiteApp.appContext.assets.open(name).use { input ->
                    FileOutputStream(tmpFile).use { output -> input.copyTo(output) }
                }
                installModule(tmpFile)
                tmpFile.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
