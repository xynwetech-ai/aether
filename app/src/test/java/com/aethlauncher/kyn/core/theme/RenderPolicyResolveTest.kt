package com.aethlauncher.kyn.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderPolicyResolveTest {

    private fun resolve(
        safe: Boolean = false,
        quality: RenderQuality = RenderQuality.HIGH,
        animations: Boolean = true,
        reduced: Boolean = false,
        lowRam: Boolean = false
    ) = RenderPolicy.resolve(safe, quality, animations, reduced, lowRam)

    @Test
    fun safeMode_winsOverEverything() {
        assertEquals(RenderPolicy.Safe, resolve(safe = true, quality = RenderQuality.HIGH))
    }

    @Test
    fun batterySaver_disablesBlurAndAnimations() {
        val policy = resolve(quality = RenderQuality.BATTERY_SAVER)
        assertFalse(policy.blurEnabled)
        assertFalse(policy.animationsEnabled)
    }

    @Test
    fun reducedMotion_disablesAnimationsButKeepsBlur() {
        val policy = resolve(reduced = true)
        assertFalse(policy.animationsEnabled)
        assertTrue(policy.blurEnabled)
    }

    @Test
    fun userAnimationSwitch_isRespected() {
        assertFalse(resolve(animations = false).animationsEnabled)
        assertTrue(resolve(animations = true).animationsEnabled)
    }

    @Test
    fun auto_onLowRamDevice_behavesLikeBatterySaver() {
        val policy = resolve(quality = RenderQuality.AUTO, lowRam = true)
        assertFalse(policy.blurEnabled)
        assertTrue(resolve(quality = RenderQuality.AUTO, lowRam = false).blurEnabled)
    }

    @Test
    fun balanced_usesLighterBlurThanHigh() {
        val high = resolve(quality = RenderQuality.HIGH)
        val balanced = resolve(quality = RenderQuality.BALANCED)
        assertTrue(balanced.blurWidth < high.blurWidth)
    }
}
