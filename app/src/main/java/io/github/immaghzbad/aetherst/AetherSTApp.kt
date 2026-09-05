package io.github.immaghzbad.aetherst

import android.app.Application
import androidx.annotation.Keep
import io.github.immaghzbad.aetherst.core.AutoConnectManager
import io.github.immaghzbad.aetherst.core.ConnectionController
import io.github.immaghzbad.aetherst.service.AetherWidgetProvider
import io.github.immaghzbad.aetherst.shared.data.LogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

@Keep
class AetherSTApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
        observeStatusForWidgets()

        io.github.immaghzbad.aetherst.shared.platform.Bridge.submitLoginCode = { code ->
            ConnectionController.getInstance(this).submitLoginCode(code)
        }

        handleCrashRecoveryAndAutoConnect()
    }

    private fun handleCrashRecoveryAndAutoConnect() {
        applicationScope.launch {
            delay(1500L)
            // Crash recovery first so a crash-driven connect isn't duplicated
            // by the regular app-start auto-connect below.
            val recovered = try {
                AutoConnectManager.handleCrashRecovery(this@AetherSTApp)
            } catch (e: Exception) {
                LogRepository.w("[AutoConnect] Crash recovery failed: ${e.message}")
                false
            }
            if (!recovered) {
                try {
                    AutoConnectManager.handleAppStart(this@AetherSTApp)
                } catch (e: Exception) {
                    LogRepository.w("[AutoConnect] App-start auto-connect failed: ${e.message}")
                }
            }
        }
    }

    private fun observeStatusForWidgets() {
        applicationScope.launch {
            ConnectionController.status.collect {
                AetherWidgetProvider.updateAllWidgets(this@AetherSTApp)
            }
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()
                // Never log secrets: only thread name + exception class/message.
                val crashLog = "Thread: ${thread.name}\n\nException: ${throwable.javaClass.name}: ${throwable.localizedMessage}\n\nStack Trace:\n$stackTrace"
                val file = File(cacheDir, "last_crash.log")
                file.writeText(crashLog.take(32_768))

                // Persist recovery flags synchronously (commit, not apply) so
                // they survive process death. Booleans only — no secrets.
                AutoConnectManager.recordCrashAndGetRecoveryFlags(this)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
