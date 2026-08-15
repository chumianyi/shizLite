package com.chumian.shizlite.module

import com.chumian.shizlite.core.ShellExecutor
import com.chumian.shizlite.core.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ModuleApi {

    suspend fun exec(command: String): ShellResult = withContext(Dispatchers.IO) {
        ShellExecutor.execute(command)
    }

    suspend fun getprop(key: String): String {
        return exec("getprop $key").stdout.trim()
    }

    suspend fun setprop(key: String, value: String): ShellResult {
        return exec("setprop $key $value")
    }

    suspend fun pmDisable(packageName: String): ShellResult {
        return exec("pm disable-user --user 0 $packageName")
    }

    suspend fun pmEnable(packageName: String): ShellResult {
        return exec("pm enable $packageName")
    }

    suspend fun pmListPackages(): String {
        return exec("pm list packages").stdout
    }

    suspend fun clearCache(packageName: String): ShellResult {
        return exec("pm trim-caches 999999999999")
    }

    suspend fun setAnimationScale(scale: Float): ShellResult {
        return exec("settings put global window_animation_scale $scale && settings put global transition_animation_scale $scale && settings put global animator_duration_scale $scale")
    }

    suspend fun writeSysctl(path: String, value: String): ShellResult {
        return exec("echo $value > $path")
    }

    suspend fun readSysctl(path: String): String {
        return exec("cat $path").stdout.trim()
    }

    suspend fun modifyHosts(content: String): ShellResult {
        return exec("echo '$content' > /system/etc/hosts")
    }

    suspend fun setCpuGovernor(governor: String): ShellResult {
        return exec("echo $governor > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
    }

    suspend fun setResolution(width: Int, height: Int): ShellResult {
        return exec("wm size ${width}x${height}")
    }

    suspend fun setDensity(dpi: Int): ShellResult {
        return exec("wm density $dpi")
    }

    suspend fun getSelinuxStatus(): String {
        return exec("getenforce").stdout.trim()
    }

    suspend fun setSelinux(mode: String): ShellResult {
        return exec("setenforce $mode")
    }
}
