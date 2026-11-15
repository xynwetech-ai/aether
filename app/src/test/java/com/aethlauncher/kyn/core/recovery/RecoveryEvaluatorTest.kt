package com.aethlauncher.kyn.core.recovery

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryEvaluatorTest {

    private val now = 1_000_000L

    @Test
    fun noEvents_isNormal() {
        val result = RecoveryEvaluator.evaluate(emptyList(), emptyList(), now)
        assertEquals(RecoveryDecision.Normal, result)
    }

    @Test
    fun twoCrashesWithinWindow_requiresRecovery() {
        val result = RecoveryEvaluator.evaluate(listOf(now - 6_000, now - 1_000), emptyList(), now)
        assertEquals(RecoveryDecision.Required(RecoveryReason.REPEATED_CRASHES, 2), result)
    }

    @Test
    fun twoCrashesFarApart_isNormal() {
        val result = RecoveryEvaluator.evaluate(listOf(now - 60_000, now - 1_000), emptyList(), now)
        assertEquals(RecoveryDecision.Normal, result)
    }

    @Test
    fun crashesOutsideLookback_areIgnored() {
        val old = now - 400_000
        val result = RecoveryEvaluator.evaluate(listOf(old, old + 1_000), emptyList(), now)
        assertEquals(RecoveryDecision.Normal, result)
    }

    @Test
    fun threeUnfinishedStartups_requiresRecovery() {
        val result = RecoveryEvaluator.evaluate(
            emptyList(),
            listOf(now - 20_000, now - 10_000, now - 1_000),
            now
        )
        assertEquals(RecoveryDecision.Required(RecoveryReason.SUSPICIOUS_STARTUP_LOOP, 3), result)
    }

    @Test
    fun twoUnfinishedStartups_isNormalWithDefaultThreshold() {
        val result = RecoveryEvaluator.evaluate(emptyList(), listOf(now - 10_000, now - 1_000), now)
        assertEquals(RecoveryDecision.Normal, result)
    }

    @Test
    fun customThreshold_isRespected() {
        val config = RecoveryConfig(confirmedCrashThreshold = 3)
        val result = RecoveryEvaluator.evaluate(listOf(now - 3_000, now - 1_000), emptyList(), now, config)
        assertEquals(RecoveryDecision.Normal, result)
    }

    @Test
    fun futureTimestamps_areIgnored() {
        val result = RecoveryEvaluator.evaluate(listOf(now + 1_000, now + 2_000), emptyList(), now)
        assertEquals(RecoveryDecision.Normal, result)
    }
}
