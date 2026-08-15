package com.chumian.shizlite.core

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.chumian.shizlite.ShizLiteApp
import org.json.JSONArray
import org.json.JSONObject

data class AppPermission(
    val packageName: String,
    val appName: String,
    val granted: Boolean,
    val grantedAt: Long
)

data class TerminalPermission(
    val terminalPackage: String,
    val granted: Boolean,
    val grantedAt: Long
)

object PermissionManager {

    private const val PREFS_NAME = "shizlite_permissions"
    private const val KEY_APPS = "authorized_apps"
    private const val KEY_TERMINALS = "authorized_terminals"

    private val prefs: SharedPreferences =
        ShizLiteApp.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAuthorizedApps(): List<AppPermission> {
        val json = prefs.getString(KEY_APPS, "[]") ?: "[]"
        val array = JSONArray(json)
        val pm = ShizLiteApp.appContext.packageManager
        val list = mutableListOf<AppPermission>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val pkg = obj.getString("packageName")
            val name = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (e: Exception) { pkg }
            list.add(AppPermission(pkg, name, obj.getBoolean("granted"), obj.getLong("grantedAt")))
        }
        return list
    }

    fun grantApp(packageName: String) {
        val apps = getAuthorizedApps().toMutableList()
        apps.removeAll { it.packageName == packageName }
        apps.add(AppPermission(packageName, packageName, true, System.currentTimeMillis()))
        saveApps(apps)
    }

    fun revokeApp(packageName: String) {
        val apps = getAuthorizedApps().filter { it.packageName != packageName }
        saveApps(apps)
    }

    fun isAppAuthorized(packageName: String): Boolean {
        return getAuthorizedApps().any { it.packageName == packageName && it.granted }
    }

    private fun saveApps(apps: List<AppPermission>) {
        val array = JSONArray()
        apps.forEach {
            array.put(JSONObject().apply {
                put("packageName", it.packageName)
                put("granted", it.granted)
                put("grantedAt", it.grantedAt)
            })
        }
        prefs.edit().putString(KEY_APPS, array.toString()).apply()
    }

    fun getAuthorizedTerminals(): List<TerminalPermission> {
        val json = prefs.getString(KEY_TERMINALS, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<TerminalPermission>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(TerminalPermission(obj.getString("terminalPackage"), obj.getBoolean("granted"), obj.getLong("grantedAt")))
        }
        return list
    }

    fun grantTerminal(terminalPackage: String) {
        val terminals = getAuthorizedTerminals().toMutableList()
        terminals.removeAll { it.terminalPackage == terminalPackage }
        terminals.add(TerminalPermission(terminalPackage, true, System.currentTimeMillis()))
        saveTerminals(terminals)
    }

    fun revokeTerminal(terminalPackage: String) {
        val terminals = getAuthorizedTerminals().filter { it.terminalPackage != terminalPackage }
        saveTerminals(terminals)
    }

    fun isTerminalAuthorized(terminalPackage: String): Boolean {
        return getAuthorizedTerminals().any { it.terminalPackage == terminalPackage && it.granted }
    }

    private fun saveTerminals(terminals: List<TerminalPermission>) {
        val array = JSONArray()
        terminals.forEach {
            array.put(JSONObject().apply {
                put("terminalPackage", it.terminalPackage)
                put("granted", it.granted)
                put("grantedAt", it.grantedAt)
            })
        }
        prefs.edit().putString(KEY_TERMINALS, array.toString()).apply()
    }

    fun getInstalledApps(): List<Pair<String, String>> {
        val pm = ShizLiteApp.appContext.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA).map {
            Pair(it.packageName, pm.getApplicationLabel(it).toString())
        }.sortedBy { it.second }
    }
}
