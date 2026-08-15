package com.chumian.shizlite.core

import android.content.Context
import android.content.SharedPreferences
import com.chumian.shizlite.ShizLiteApp

object ActivationManager {

    enum class ActivationMethod { NONE, WIRELESS_DEBUGGING, ROOT }

    private const val PREFS_NAME = "shizlite_activation"
    private const val KEY_METHOD = "activation_method"
    private const val KEY_ACTIVE = "is_active"

    private val prefs: SharedPreferences =
        ShizLiteApp.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var activationMethod: ActivationMethod
        get() {
            val name = prefs.getString(KEY_METHOD, ActivationMethod.NONE.name) ?: "NONE"
            return ActivationMethod.valueOf(name)
        }
        set(value) = prefs.edit().putString(KEY_METHOD, value.name).apply()

    var isActive: Boolean
        get() = prefs.getBoolean(KEY_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_ACTIVE, value).apply()

    fun activateViaRoot(): Boolean {
        val available = ShellExecutor.checkRootAvailable()
        if (available) {
            activationMethod = ActivationMethod.ROOT
            isActive = true
            ShellExecutor.currentLevel = ShellExecutor.PrivilegeLevel.ROOT
        }
        return available
    }

    fun activateViaWirelessDebugging(port: Int = 0): Boolean {
        activationMethod = ActivationMethod.WIRELESS_DEBUGGING
        isActive = true
        ShellExecutor.currentLevel = ShellExecutor.PrivilegeLevel.SHIZUKU
        return true
    }

    fun deactivate() {
        isActive = false
        activationMethod = ActivationMethod.NONE
        ShellExecutor.currentLevel = ShellExecutor.PrivilegeLevel.NORMAL
    }

    fun getPrivilegeLevel(): ShellExecutor.PrivilegeLevel {
        return when (activationMethod) {
            ActivationMethod.ROOT -> ShellExecutor.PrivilegeLevel.ROOT
            ActivationMethod.WIRELESS_DEBUGGING -> ShellExecutor.PrivilegeLevel.SHIZUKU
            ActivationMethod.NONE -> ShellExecutor.PrivilegeLevel.NORMAL
        }
    }
}
