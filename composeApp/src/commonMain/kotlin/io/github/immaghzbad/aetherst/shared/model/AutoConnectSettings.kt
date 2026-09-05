package io.github.immaghzbad.aetherst.shared.model

data class AutoConnectSettings(
    val autoConnectOnStart: Boolean = false,
    val autoConnectOnBoot: Boolean = false,
    val autoConnectOnNetwork: Boolean = false,
    val autoRestartOnCrash: Boolean = false,
    val autoConnectAfterCrash: Boolean = false
) {
    companion object {
        private const val PREF_PREFIX = "auto_connect_"
        const val PREF_AUTO_CONNECT_ON_START = "${PREF_PREFIX}on_start"
        const val PREF_AUTO_CONNECT_ON_BOOT = "${PREF_PREFIX}on_boot"
        const val PREF_AUTO_CONNECT_ON_NETWORK = "${PREF_PREFIX}on_network"
        const val PREF_AUTO_RESTART_ON_CRASH = "${PREF_PREFIX}restart_crash"
        const val PREF_AUTO_CONNECT_AFTER_CRASH = "${PREF_PREFIX}after_crash"
        const val PREF_MANUAL_DISCONNECT = "${PREF_PREFIX}manual_disconnect"
        const val PREF_CRASH_COUNT = "${PREF_PREFIX}crash_count"
        const val PREF_CRASH_WINDOW_START = "${PREF_PREFIX}crash_window_start"
        const val PREF_CRASH_RESTART_PENDING = "${PREF_PREFIX}crash_restart_pending"
        const val PREF_CRASH_AUTOCONNECT_PENDING = "${PREF_PREFIX}crash_autoconnect_pending"

        const val MAX_CRASH_RETRIES = 3
        const val CRASH_WINDOW_MS = 60_000L
        const val NETWORK_DEBOUNCE_MS = 3000L
    }
}
