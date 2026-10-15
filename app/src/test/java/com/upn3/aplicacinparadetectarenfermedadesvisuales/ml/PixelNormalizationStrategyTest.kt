package com.upn3.aplicacinparadetectarenfermedadesvisuales.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class PixelNormalizationStrategyTest {

    private val delta = 0.0001f

    @Test
    fun `raw strategy passes pixel values through unchanged`() {
        val (r, g, b) = Raw0To255Normalization.normalize(0, 128, 255)
        assertEquals(0f, r, delta)
        assertEquals(128f, g, delta)
        assertEquals(255f, b, delta)
    }

    @Test
    fun `zero-to-one strategy scales the full byte range into 0 to 1`() {
        val (rMin, gMin, bMin) = ZeroToOneNormalization.normalize(0, 0, 0)
        assertEquals(0f, rMin, delta)
        assertEquals(0f, gMin, delta)
        assertEquals(0f, bMin, delta)

        val (rMax, gMax, bMax) = ZeroToOneNormalization.normalize(255, 255, 255)
        assertEquals(1f, rMax, delta)
        assertEquals(1f, gMax, delta)
        assertEquals(1f, bMax, delta)
    }

    @Test
    fun `minus-one-to-one strategy centers the byte range at zero`() {
        val (rMin, _, _) = MinusOneToOneNormalization.normalize(0, 0, 0)
        assertEquals(-1f, rMin, delta)

        val (rMax, _, _) = MinusOneToOneNormalization.normalize(255, 255, 255)
        assertEquals(1f, rMax, delta)

        val (rMid, _, _) = MinusOneToOneNormalization.normalize(128, 128, 128)
        assertEquals(0f, rMid, 0.01f)
    }

    @Test
    fun `channels are mapped independently, not averaged together`() {
        val (r, g, b) = ZeroToOneNormalization.normalize(255, 0, 128)
        assertEquals(1f, r, delta)
        assertEquals(0f, g, delta)
        assertEquals(128 / 255f, b, delta)
    }
}
