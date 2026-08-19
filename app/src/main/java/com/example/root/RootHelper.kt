package com.example.root

import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootHelper {
    private const val TAG = "RootHelper"

    init {
        // Configure libsu defaults
        Shell.enableVerboseLogging = false
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.isAppGrantedRoot() == true || Shell.getShell().isRoot
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed: ${e.message}")
            false
        }
    }

    suspend fun runRootCommand(vararg commands: String): CommandResult = withContext(Dispatchers.IO) {
        val cmdList = commands.toList()
        try {
            val shell = Shell.getShell()
            if (!shell.isRoot) {
                return@withContext CommandResult(
                    isSuccess = false,
                    output = emptyList(),
                    errorMessage = "Root permission not granted"
                )
            }
            val result = Shell.cmd(*commands).exec()
            CommandResult(
                isSuccess = result.isSuccess,
                output = result.out,
                errorMessage = if (!result.isSuccess) result.err.joinToString("\n") else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed: ${e.message}")
            CommandResult(
                isSuccess = false,
                output = emptyList(),
                errorMessage = e.message ?: "Unknown shell error"
            )
        }
    }

    suspend fun readSysfs(path: String): String? = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("cat $path 2>/dev/null").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                result.out.firstOrNull()?.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkNodeExists(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("[ -e $path ] && echo 1 || echo 0").exec()
            result.out.firstOrNull()?.trim() == "1"
        } catch (e: Exception) {
            false
        }
    }
}

data class CommandResult(
    val isSuccess: Boolean,
    val output: List<String> = emptyList(),
    val errorMessage: String? = null
)
