package com.aethlauncher.kyn.core.wallpaper

/** Box blur terpisah (horizontal lalu vertikal) pada piksel ARGB. Murni Kotlin supaya bisa diuji. */
object BoxBlur {

    fun blur(pixels: IntArray, width: Int, height: Int, radius: Int, passes: Int = 3): IntArray {
        require(pixels.size == width * height) { "Ukuran piksel tidak cocok" }
        if (radius <= 0 || width <= 0 || height <= 0) return pixels.copyOf()

        var src = pixels.copyOf()
        var dst = IntArray(pixels.size)
        repeat(passes) {
            for (y in 0 until height) blurLine(src, dst, y * width, 1, width, radius)
            val afterHorizontal = src
            src = dst
            dst = afterHorizontal
            for (x in 0 until width) blurLine(src, dst, x, width, height, radius)
            val afterVertical = src
            src = dst
            dst = afterVertical
        }
        return src
    }

    private fun blurLine(src: IntArray, dst: IntArray, start: Int, stride: Int, length: Int, radius: Int) {
        val div = 2 * radius + 1
        var a = 0
        var r = 0
        var g = 0
        var b = 0
        for (i in -radius..radius) {
            val p = src[start + i.coerceIn(0, length - 1) * stride]
            a += p ushr 24
            r += (p shr 16) and 0xFF
            g += (p shr 8) and 0xFF
            b += p and 0xFF
        }
        for (i in 0 until length) {
            dst[start + i * stride] =
                ((a / div) shl 24) or ((r / div) shl 16) or ((g / div) shl 8) or (b / div)
            val add = src[start + (i + radius + 1).coerceAtMost(length - 1) * stride]
            val sub = src[start + (i - radius).coerceAtLeast(0) * stride]
            a += (add ushr 24) - (sub ushr 24)
            r += ((add shr 16) and 0xFF) - ((sub shr 16) and 0xFF)
            g += ((add shr 8) and 0xFF) - ((sub shr 8) and 0xFF)
            b += (add and 0xFF) - (sub and 0xFF)
        }
    }
}
