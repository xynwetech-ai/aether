package com.aethlauncher.kyn.core.wallpaper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoxBlurTest {

    @Test
    fun uniformImage_staysUniform() {
        val color = 0xFF336699.toInt()
        val pixels = IntArray(16 * 12) { color }
        val result = BoxBlur.blur(pixels, 16, 12, 3)
        assertTrue(result.all { it == color })
    }

    @Test
    fun sizeIsPreserved() {
        val pixels = IntArray(10 * 7) { 0xFF000000.toInt() }
        assertEquals(pixels.size, BoxBlur.blur(pixels, 10, 7, 2).size)
    }

    @Test
    fun zeroRadius_returnsCopy() {
        val pixels = IntArray(9) { it }
        val result = BoxBlur.blur(pixels, 3, 3, 0)
        assertTrue(pixels.contentEquals(result))
    }

    @Test
    fun blur_reducesContrast() {
        // Papan catur hitam-putih harus menjadi lebih abu-abu setelah di-blur.
        val width = 16
        val height = 16
        val pixels = IntArray(width * height) { i ->
            val x = i % width
            val y = i / width
            if ((x + y) % 2 == 0) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        val blurred = BoxBlur.blur(pixels, width, height, 2)
        val centre = blurred[(height / 2) * width + width / 2] and 0xFF
        assertTrue("nilai tengah harus mendekati abu-abu", centre in 60..200)
    }
}
