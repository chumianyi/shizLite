package moe.shizuku.manager.module

import android.content.Context
import android.content.SharedPreferences
import moe.shizuku.manager.application
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

object ModuleManager {

    private const val PREFS_NAME = "shizlite_modules"
    private const val KEY_MODULES = "installed_modules"

    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val modulesDir: File
        get() = File(application.filesDir, "modules").apply { mkdirs() }

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

    fun installModule(slmFile: File): Result<Module> {
        return try {
            val zipFile = ZipFile(slmFile)
            val entry = zipFile.getEntry("module.json")
                ?: return Result.failure(Exception("module.json not found in package"))
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
            val installed = module.copy(activated = false, installPath = targetDir.absolutePath)
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

    fun runModule(module: Module, action: String = "main"): Pair<Int, String> {
        if (!module.activated) {
            return Pair(-1, "Module not activated")
        }
        val scriptFile = File(module.installPath, module.entryScript)
        if (!scriptFile.exists()) {
            return Pair(-1, "Entry script not found")
        }
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", scriptFile.absolutePath, action))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            Pair(exitCode, output + error)
        } catch (e: Exception) {
            Pair(-1, e.message ?: "Execution failed")
        }
    }

    fun installPrebuiltModules(context: Context) {
        listOf("module_optimizer.slm", "module_root_optimizer.slm").forEach { name ->
            try {
                val tmpFile = File(context.cacheDir, name)
                context.assets.open(name).use { input ->
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
