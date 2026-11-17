package com.aethlauncher.kyn.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PIN tidak pernah disimpan sebagai teks: hanya hash PBKDF2 dengan salt acak. */
object PinHasher {
    private const val ITERATIONS = 20_000
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16

    fun newSalt(): String {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(pin: String, saltBase64: String): String {
        val salt = Base64.getDecoder().decode(saltBase64)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_BITS)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(key)
    }

    fun verify(pin: String, saltBase64: String, expectedHash: String): Boolean {
        if (saltBase64.isEmpty() || expectedHash.isEmpty()) return false
        val actual = hash(pin, saltBase64)
        return MessageDigest.isEqual(actual.toByteArray(), expectedHash.toByteArray())
    }
}
