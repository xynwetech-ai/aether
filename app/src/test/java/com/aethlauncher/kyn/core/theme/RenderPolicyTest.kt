package com.aethlauncher.kyn.core.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderPolicyTest {

    @Test
    fun safeMode_disablesAllEffects() {
        val policy = RenderPolicy.forSafeMode(true)
        assertFalse(policy.animationsEnabled)
        assertFalse(policy.blurEnabled)
        assertFalse(policy.dynamicColorEnabled)
        assertFalse(policy.experimentalEnabled)
        assertFalse(policy.widgetsEnabled)
    }

    @Test
    fun standardMode_enablesAllEffects() {
        val policy = RenderPolicy.forSafeMode(false)
        assertTrue(policy.animationsEnabled)
        assertTrue(policy.blurEnabled)
        assertTrue(policy.dynamicColorEnabled)
        assertTrue(policy.experimentalEnabled)
        assertTrue(policy.widgetsEnabled)
    }
}
