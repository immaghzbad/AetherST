package io.github.immaghzbad.aetherst.core

import android.content.Context
import io.github.immaghzbad.aetherst.platform.PlatformContext
import io.github.immaghzbad.aetherst.platform.getSettings
import io.github.immaghzbad.aetherst.service.AetherVpnService
import io.github.immaghzbad.aetherst.shared.data.LogRepository
import io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings
import io.github.immaghzbad.aetherst.shared.model.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AutoConnectManager {

    private val PREF_AUTO_CONNECT_ON_START = AutoConnectSettings.PREF_AUTO_CONNECT_ON_START
    private val PREF_AUTO_CONNECT_ON_BOOT = AutoConnectSettings.PREF_AUTO_CONNECT_ON_BOOT
    private val PREF_AUTO_CONNECT_ON_NETWORK = AutoConnectSettings.PREF_AUTO_CONNECT_ON_NETWORK
    private val PREF_AUTO_RESTART_ON_CRASH = AutoConnectSettings.PREF_AUTO_RESTART_ON_CRASH
    private val PREF_AUTO_CONNECT_AFTER_CRASH = AutoConnectSettings.PREF_AUTO_CONNECT_AFTER_CRASH
    private val PREF_MANUAL_DISCONNECT = AutoConnectSettings.PREF_MANUAL_DISCONNECT
    private val PREF_CRASH_COUNT = AutoConnectSettings.PREF_CRASH_COUNT
    private val PREF_CRASH_WINDOW_START = AutoConnectSettings.PREF_CRASH_WINDOW_START
    private val PREF_CRASH_RESTART_PENDING = AutoConnectSettings.PREF_CRASH_RESTART_PENDING
    private val PREF_CRASH_AUTOCONNECT_PENDING = AutoConnectSettings.PREF_CRASH_AUTOCONNECT_PENDING

    private const val NETWORK_DEBOUNCE_MS = AutoConnectSettings.NETWORK_DEBOUNCE_MS

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var networkDebounceJob: Job? = null

    fun loadSettings(context: Context): AutoConnectSettings {
        val settings = getSettings(PlatformContext(context))
        return AutoConnectSettings(
            autoConnectOnStart = settings.getBoolean(PREF_AUTO_CONNECT_ON_START, false),
            autoConnectOnBoot = settings.getBoolean(PREF_AUTO_CONNECT_ON_BOOT, false),
            autoConnectOnNetwork = settings.getBoolean(PREF_AUTO_CONNECT_ON_NETWORK, false),
            autoRestartOnCrash = settings.getBoolean(PREF_AUTO_RESTART_ON_CRASH, false),
            autoConnectAfterCrash = settings.getBoolean(PREF_AUTO_CONNECT_AFTER_CRASH, false)
        )
    }

    fun saveSettings(context: Context, s: AutoConnectSettings) {
        val prefs = getSettings(PlatformContext(context))
        prefs.putBoolean(PREF_AUTO_CONNECT_ON_START, s.autoConnectOnStart)
        prefs.putBoolean(PREF_AUTO_CONNECT_ON_BOOT, s.autoConnectOnBoot)
        prefs.putBoolean(PREF_AUTO_CONNECT_ON_NETWORK, s.autoConnectOnNetwork)
        prefs.putBoolean(PREF_AUTO_RESTART_ON_CRASH, s.autoRestartOnCrash)
        prefs.putBoolean(PREF_AUTO_CONNECT_AFTER_CRASH, s.autoConnectAfterCrash)
    }

    fun setManualDisconnect(context: Context, manual: Boolean) {
        val prefs = getSettings(PlatformContext(context))
        prefs.putBoolean(PREF_MANUAL_DISCONNECT, manual)
    }

    fun isManualDisconnect(context: Context): Boolean {
        val prefs = getSettings(PlatformContext(context))
        return prefs.getBoolean(PREF_MANUAL_DISCONNECT, false)
    }

    private fun isActiveOrBusy(status: ConnectionStatus): Boolean {
        return status == ConnectionStatus.RUNNING ||
            status == ConnectionStatus.TUN_ACTIVE ||
            status == ConnectionStatus.STARTING ||
            status == ConnectionStatus.VALIDATING ||
            status == ConnectionStatus.DATAPLANE_VALIDATED ||
            status == ConnectionStatus.SOCKS_READY ||
            status == ConnectionStatus.RECONNECTING
    }

    fun handleAppStart(context: Context) {
        val s = loadSettings(context)
        if (!s.autoConnectOnStart) return
        if (isManualDisconnect(context)) {
            LogRepository.i("[AutoConnect] Skipping: manual disconnect active")
            return
        }
        if (isActiveOrBusy(ConnectionController.status.value)) return
        LogRepository.i("[AutoConnect] Auto-connecting on app start")
        AetherVpnService.startVpn(context)
    }

    fun handleBootCompleted(context: Context) {
        val s = loadSettings(context)
        if (!s.autoConnectOnBoot) return
        if (isManualDisconnect(context)) {
            LogRepository.i("[AutoConnect] Skipping boot: manual disconnect active")
            return
        }
        LogRepository.i("[AutoConnect] Auto-connecting on boot")
        AetherVpnService.startVpn(context)
    }

    fun handleNetworkChange(context: Context) {
        val s = loadSettings(context)
        if (!s.autoConnectOnNetwork) return
        if (isManualDisconnect(context)) return
        if (isActiveOrBusy(ConnectionController.status.value)) return

        networkDebounceJob?.cancel()
        networkDebounceJob = scope.launch {
            delay(NETWORK_DEBOUNCE_MS)
            if (isActiveOrBusy(ConnectionController.status.value)) return@launch
            if (isManualDisconnect(context)) return@launch
            // Re-read settings after debounce: user may have disabled meanwhile.
            if (!loadSettings(context).autoConnectOnNetwork) return@launch
            LogRepository.i("[AutoConnect] Auto-connecting on network change")
            AetherVpnService.startVpn(context)
        }
    }

    /**
     * Called from the crash handler. Records the crash, applies the crash-loop
     * guard, and persists pending-recovery flags synchronously so the next
     * launch can act on them. Returns (restartAllowed, autoConnectWanted).
     */
    fun recordCrashAndGetRecoveryFlags(context: Context): Pair<Boolean, Boolean> {
        val prefs = getSettings(PlatformContext(context))
        val autoRestartOnCrash = prefs.getBoolean(PREF_AUTO_RESTART_ON_CRASH, false)
        val autoConnectAfterCrash = prefs.getBoolean(PREF_AUTO_CONNECT_AFTER_CRASH, false)
        val now = System.currentTimeMillis()
        val decision = CrashRecoveryPolicy.nextState(
            crashCount = prefs.getInt(PREF_CRASH_COUNT, 0),
            windowStart = prefs.getLong(PREF_CRASH_WINDOW_START, 0L),
            now = now
        )
        prefs.putInt(PREF_CRASH_COUNT, decision.newCrashCount)
        prefs.putLong(PREF_CRASH_WINDOW_START, decision.newWindowStart)

        if (!autoRestartOnCrash) return Pair(false, false)

        if (decision.crashLoopDetected) {
            LogRepository.e("[AutoConnect] Crash loop detected (${decision.newCrashCount} in window). Disabling auto-restart.")
            prefs.putBoolean(PREF_AUTO_RESTART_ON_CRASH, false)
            prefs.putInt(PREF_CRASH_COUNT, 0)
            persistPendingFlagsSync(context, restartPending = false, autoConnectPending = false)
            return Pair(false, false)
        }

        LogRepository.i("[AutoConnect] Crash recovery: attempt ${decision.newCrashCount}/${AutoConnectSettings.MAX_CRASH_RETRIES}")
        persistPendingFlagsSync(context, restartPending = true, autoConnectPending = autoConnectAfterCrash)
        return Pair(true, autoConnectAfterCrash)
    }

    fun shouldRecoverFromCrash(context: Context): Boolean {
        return recordCrashAndGetRecoveryFlags(context).first
    }

    fun shouldAutoConnectAfterCrash(context: Context): Boolean {
        return loadSettings(context).autoConnectAfterCrash
    }

    /**
     * Called once on next app start. Consumes (reads + clears) the pending
     * flags written by the crash handler. Returns (restartWasPending,
     * autoConnectWasPending).
     */
    fun consumeCrashPending(context: Context): Pair<Boolean, Boolean> {
        val prefs = getSettings(PlatformContext(context))
        val restart = prefs.getBoolean(PREF_CRASH_RESTART_PENDING, false)
        val autoConnect = prefs.getBoolean(PREF_CRASH_AUTOCONNECT_PENDING, false)
        if (restart || autoConnect) {
            prefs.putBoolean(PREF_CRASH_RESTART_PENDING, false)
            prefs.putBoolean(PREF_CRASH_AUTOCONNECT_PENDING, false)
        }
        return Pair(restart, autoConnect)
    }

    /**
     * Handles post-crash recovery on app start. Must be called before
     * [handleAppStart] so a crash-driven connect isn't duplicated.
     * Returns true if it triggered a connect.
     */
    fun handleCrashRecovery(context: Context): Boolean {
        val (restartPending, autoConnectPending) = consumeCrashPending(context)
        if (!restartPending) return false
        val s = loadSettings(context)
        if (!s.autoRestartOnCrash) {
            LogRepository.i("[AutoConnect] Crash restart was pending but auto-restart is now disabled; skipping")
            return false
        }
        if (!autoConnectPending || !s.autoConnectAfterCrash) {
            LogRepository.i("[AutoConnect] App restarted after crash (no auto-connect requested)")
            return false
        }
        if (isManualDisconnect(context)) {
            LogRepository.i("[AutoConnect] Skipping post-crash connect: manual disconnect active")
            return false
        }
        if (isActiveOrBusy(ConnectionController.status.value)) return true
        LogRepository.i("[AutoConnect] Auto-connecting after crash restart")
        AetherVpnService.startVpn(context)
        return true
    }

    fun clearCrashCount(context: Context) {
        val prefs = getSettings(PlatformContext(context))
        prefs.putInt(PREF_CRASH_COUNT, 0)
        prefs.putLong(PREF_CRASH_WINDOW_START, 0L)
    }

    fun clearManualDisconnect(context: Context) {
        setManualDisconnect(context, false)
    }

    /**
     * Synchronous commit of pending flags. The Settings wrapper uses apply()
     * (async) which may not survive process death, so the crash path writes
     * directly with commit(). No secrets are stored here — booleans only.
     */
    private fun persistPendingFlagsSync(context: Context, restartPending: Boolean, autoConnectPending: Boolean) {
        try {
            context.getSharedPreferences("aether_settings", Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_CRASH_RESTART_PENDING, restartPending)
                .putBoolean(PREF_CRASH_AUTOCONNECT_PENDING, autoConnectPending)
                .commit()
        } catch (_: Exception) {
        }
    }
}
