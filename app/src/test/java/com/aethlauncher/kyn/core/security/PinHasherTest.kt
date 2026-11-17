package com.aethlauncher.kyn.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    @Test
    fun correctPin_verifies() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertTrue(PinHasher.verify("1234", salt, hash))
    }

    @Test
    fun wrongPin_isRejected() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertFalse(PinHasher.verify("4321", salt, hash))
    }

    @Test
    fun sameInput_givesSameHash_butDifferentSaltDiffers() {
        val salt = PinHasher.newSalt()
        assertEquals(PinHasher.hash("2468", salt), PinHasher.hash("2468", salt))
        assertNotEquals(PinHasher.hash("2468", salt), PinHasher.hash("2468", PinHasher.newSalt()))
    }

    @Test
    fun hashIsNeverThePlainPin() {
        val salt = PinHasher.newSalt()
        assertNotEquals("1234", PinHasher.hash("1234", salt))
    }

    @Test
    fun emptyStoredValues_neverVerify() {
        assertFalse(PinHasher.verify("1234", "", ""))
    }
}
