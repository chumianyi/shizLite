package com.chumian.shizlite.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

object ShellExecutor {

    enum class PrivilegeLevel { NORMAL, SHIZUKU, ROOT }

    var currentLevel: PrivilegeLevel = PrivilegeLevel.NORMAL

    suspend fun execute(command: String, level: PrivilegeLevel = currentLevel): ShellResult = withContext(Dispatchers.IO) {
        val processBuilder = when (level) {
            PrivilegeLevel.ROOT -> ProcessBuilder("su")
            else -> ProcessBuilder("sh")
        }
        processBuilder.redirectErrorStream(false)
        val process = processBuilder.start()
        val outputStream = DataOutputStream(process.outputStream)
        outputStream.writeBytes(command + "\n")
        outputStream.writeBytes("exit\n")
        outputStream.flush()
        outputStream.close()

        val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
        val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
        val exitCode = process.waitFor()
        ShellResult(exitCode, stdout, stderr)
    }

    fun checkRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    fun checkShizukuAvailable(): Boolean {
        return try {
            Class.forName("rikka.shizuku.Shizuku")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
