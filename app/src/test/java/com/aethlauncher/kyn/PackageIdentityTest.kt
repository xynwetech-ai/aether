package com.aethlauncher.kyn

import org.junit.Assert.assertEquals
import org.junit.Test

class PackageIdentityTest {

    @Test
    fun applicationId_matchesPrd() {
        assertEquals("com.aethlauncher.kyn", BuildConfig.APPLICATION_ID)
    }
}
