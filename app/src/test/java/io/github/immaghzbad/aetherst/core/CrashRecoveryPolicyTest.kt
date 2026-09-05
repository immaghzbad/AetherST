package io.github.immaghzbad.aetherst.core

import io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashRecoveryPolicyTest {

    @Test
    fun `first crash starts new window with count 1`() {
        val d = CrashRecoveryPolicy.nextState(crashCount = 0, windowStart = 0L, now = 1_000_000L)
        assertEquals(1, d.newCrashCount)
        assertEquals(1_000_000L, d.newWindowStart)
        assertFalse(d.crashLoopDetected)
    }

    @Test
    fun `crashes within window accumulate without loop up to max`() {
        val now = 1_000_000L
        val start = now - 10_000L
        val max = AutoConnectSettings.MAX_CRASH_RETRIES
        for (count in 0 until max) {
            val d = CrashRecoveryPolicy.nextState(crashCount = count, windowStart = start, now = now)
            assertEquals(count + 1, d.newCrashCount)
            assertEquals(start, d.newWindowStart)
            assertFalse(d.crashLoopDetected)
        }
    }

    @Test
    fun `crash beyond max retries triggers loop protection`() {
        val now = 1_000_000L
        val start = now - 10_000L
        val d = CrashRecoveryPolicy.nextState(
            crashCount = AutoConnectSettings.MAX_CRASH_RETRIES,
            windowStart = start,
            now = now
        )
        assertTrue(d.crashLoopDetected)
    }

    @Test
    fun `expired window resets count to 1`() {
        val now = 1_000_000L
        val expiredStart = now - AutoConnectSettings.CRASH_WINDOW_MS - 1L
        val d = CrashRecoveryPolicy.nextState(crashCount = 3, windowStart = expiredStart, now = now)
        assertEquals(1, d.newCrashCount)
        assertEquals(now, d.newWindowStart)
        assertFalse(d.crashLoopDetected)
    }

    @Test
    fun `high historical count does not loop after window expiry`() {
        val now = 5_000_000L
        val d = CrashRecoveryPolicy.nextState(crashCount = 99, windowStart = 0L, now = now)
        assertEquals(1, d.newCrashCount)
        assertFalse(d.crashLoopDetected)
    }

    @Test
    fun `auto-connect settings default to disabled`() {
        val s = AutoConnectSettings()
        assertFalse(s.autoConnectOnStart)
        assertFalse(s.autoConnectOnBoot)
        assertFalse(s.autoConnectOnNetwork)
        assertFalse(s.autoRestartOnCrash)
        assertFalse(s.autoConnectAfterCrash)
    }

    @Test
    fun `timing constants are sane`() {
        assertTrue(AutoConnectSettings.MAX_CRASH_RETRIES > 0)
        assertTrue(AutoConnectSettings.CRASH_WINDOW_MS > 0)
        assertTrue(AutoConnectSettings.NETWORK_DEBOUNCE_MS >= 1000L)
    }
}
