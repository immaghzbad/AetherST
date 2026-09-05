package io.github.immaghzbad.aetherst.core

import io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings

/**
 * Pure, platform-free crash-loop guard logic. Kept free of Android APIs so it
 * can be unit-tested on the JVM. All timestamps are epoch millis.
 */
object CrashRecoveryPolicy {

    data class Decision(
        val newCrashCount: Int,
        val newWindowStart: Long,
        val crashLoopDetected: Boolean
    )

    fun nextState(crashCount: Int, windowStart: Long, now: Long): Decision {
        val maxRetries = AutoConnectSettings.MAX_CRASH_RETRIES
        val windowMs = AutoConnectSettings.CRASH_WINDOW_MS
        val (count, start) = if (now - windowStart > windowMs) {
            Pair(1, now)
        } else {
            Pair(crashCount + 1, windowStart)
        }
        return if (count > maxRetries) {
            Decision(newCrashCount = count, newWindowStart = start, crashLoopDetected = true)
        } else {
            Decision(newCrashCount = count, newWindowStart = start, crashLoopDetected = false)
        }
    }
}
