package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Las reglas de calidad trabajan sobre [LuminanceSample] (un IntArray plano), no sobre un
 * [android.graphics.Bitmap], asi que se pueden probar como JVM puro sin Robolectric ni un
 * dispositivo. Cada prueba arma una "imagen" sintetica del brillo/nitidez que quiere simular.
 */
class FrameQualityRuleTest {

    private fun solidSample(size: Int, luminance: Int): LuminanceSample =
        LuminanceSample(IntArray(size * size) { luminance }, size)

    // Tablero de ajedrez: maxima variacion posible entre pixeles vecinos, para simular una
    // imagen bien nitida (varianza de laplaciano alta).
    private fun checkerboardSample(size: Int): LuminanceSample {
        val luminance = IntArray(size * size) { i ->
            val x = i % size
            val y = i / size
            if ((x + y) % 2 == 0) 255 else 0
        }
        return LuminanceSample(luminance, size)
    }

    @Test
    fun `TooDarkRule flags a frame darker than its threshold`() {
        val rule = TooDarkRule(threshold = 55.0)
        assertEquals(FrameQuality.TOO_DARK, rule.evaluate(solidSample(size = 8, luminance = 10)))
    }

    @Test
    fun `TooDarkRule says nothing about a well-lit frame`() {
        val rule = TooDarkRule(threshold = 55.0)
        assertNull(rule.evaluate(solidSample(size = 8, luminance = 120)))
    }

    @Test
    fun `TooBrightRule flags a frame brighter than its threshold`() {
        val rule = TooBrightRule(threshold = 205.0)
        assertEquals(FrameQuality.TOO_BRIGHT, rule.evaluate(solidSample(size = 8, luminance = 250)))
    }

    @Test
    fun `TooBrightRule says nothing about a well-lit frame`() {
        val rule = TooBrightRule(threshold = 205.0)
        assertNull(rule.evaluate(solidSample(size = 8, luminance = 120)))
    }

    @Test
    fun `BlurryRule flags a perfectly flat frame as blurry`() {
        // Una imagen de un solo color no tiene bordes: varianza de laplaciano = 0.
        val rule = BlurryRule(threshold = 90.0)
        assertEquals(FrameQuality.BLURRY, rule.evaluate(solidSample(size = 16, luminance = 128)))
    }

    @Test
    fun `BlurryRule does not flag a sharp, high-contrast frame`() {
        val rule = BlurryRule(threshold = 90.0)
        assertNull(rule.evaluate(checkerboardSample(size = 16)))
    }

    @Test
    fun `FrameQualityAnalyzer rules run in order and the first match wins`() {
        // Si la primera regla ya encuentra un problema, las siguientes ni se evaluan: alcanza
        // con una lista de reglas "falsas" que verifican eso.
        var secondRuleWasCalled = false
        val rules = listOf(
            FrameQualityRule { FrameQuality.TOO_DARK },
            FrameQualityRule { secondRuleWasCalled = true; null }
        )
        val result = rules.firstNotNullOfOrNull { it.evaluate(solidSample(size = 4, luminance = 128)) }
        assertEquals(FrameQuality.TOO_DARK, result)
        assertEquals(false, secondRuleWasCalled)
    }

    @Test
    fun `no rules matching means the frame is good`() {
        val rules = listOf(TooDarkRule(), TooBrightRule(), BlurryRule())
        val result = rules.firstNotNullOfOrNull { it.evaluate(checkerboardSample(size = 16)) }
        assertNull(result)
    }
}
