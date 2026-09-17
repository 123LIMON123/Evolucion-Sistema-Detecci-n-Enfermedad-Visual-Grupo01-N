package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera

import android.graphics.Bitmap

enum class FrameQuality {
    POSITIONING,
    TOO_DARK,
    TOO_BRIGHT,
    BLURRY,
    GOOD
}

/** Version reducida y en escala de grises de un frame, lista para que una [FrameQualityRule] la evalue. */
data class LuminanceSample(val luminance: IntArray, val size: Int)

/**
 * [Principio O - OCP] Este es el punto de extension: para agregar un chequeo nuevo (por ejemplo
 * deteccion de glare/reflejo, o "no se detecta un ojo") se escribe una clase nueva que implemente
 * esta interfaz y se agrega a la lista de [FrameQualityAnalyzer]. No hace falta tocar el codigo
 * de [FrameQualityAnalyzer] ni el de las reglas existentes (TooDarkRule, TooBrightRule, etc.):
 * la clase queda "cerrada a modificacion, abierta a extension".
 */
fun interface FrameQualityRule {
    /** Devuelve el problema detectado, o `null` si esta regla no encuentra nada malo. */
    fun evaluate(sample: LuminanceSample): FrameQuality?
}

/** Marca [FrameQuality.TOO_DARK] cuando el brillo promedio esta por debajo del umbral. */
class TooDarkRule(private val threshold: Double = 55.0) : FrameQualityRule {
    override fun evaluate(sample: LuminanceSample): FrameQuality? {
        val brightness = sample.luminance.average()
        return if (brightness < threshold) FrameQuality.TOO_DARK else null
    }
}

/** Marca [FrameQuality.TOO_BRIGHT] cuando hay demasiado brillo o reflejo (glare simple). */
class TooBrightRule(private val threshold: Double = 205.0) : FrameQualityRule {
    override fun evaluate(sample: LuminanceSample): FrameQuality? {
        val brightness = sample.luminance.average()
        return if (brightness > threshold) FrameQuality.TOO_BRIGHT else null
    }
}

/**
 * Marca [FrameQuality.BLURRY] usando la varianza de un filtro laplaciano como heuristica de
 * nitidez: una imagen nitida tiene bordes marcados (varianza alta), una borrosa los suaviza.
 */
class BlurryRule(private val threshold: Double = 90.0) : FrameQualityRule {
    override fun evaluate(sample: LuminanceSample): FrameQuality? {
        return if (laplacianVariance(sample) < threshold) FrameQuality.BLURRY else null
    }

    private fun laplacianVariance(sample: LuminanceSample): Double {
        val luminance = sample.luminance
        val size = sample.size
        val laplacian = DoubleArray(luminance.size)
        for (y in 1 until size - 1) {
            for (x in 1 until size - 1) {
                val idx = y * size + x
                laplacian[idx] = (
                    -4 * luminance[idx] +
                        luminance[idx - 1] + luminance[idx + 1] +
                        luminance[idx - size] + luminance[idx + size]
                    ).toDouble()
            }
        }
        val mean = laplacian.average()
        return laplacian.sumOf { (it - mean) * (it - mean) } / laplacian.size
    }
}

/**
 * [Principio S - SRP] Unica responsabilidad: decidir si un frame de camara tiene suficiente luz y
 * nitidez para intentar un analisis (guia de encuadre). No sabe nada de enfermedades ni del
 * modelo TFLite; solo reduce el frame a una muestra de luminancia y se la pasa a sus [rules].
 *
 * [Principio O - OCP] Cerrado a modificacion: esta clase no cambia cuando se agrega un chequeo
 * nuevo. Abierto a extension: quien la construye (ver `AppContainer`) puede pasarle una lista de
 * reglas distinta (agregar una nueva, sacar una, reordenarlas) sin editar este archivo.
 */
class FrameQualityAnalyzer(
    private val rules: List<FrameQualityRule> = listOf(TooDarkRule(), TooBrightRule(), BlurryRule())
) {

    fun analyze(bitmap: Bitmap): FrameQuality {
        val sample = sampleLuminance(bitmap)
        for (rule in rules) {
            val issue = rule.evaluate(sample)
            if (issue != null) return issue
        }
        return FrameQuality.GOOD
    }

    private fun sampleLuminance(bitmap: Bitmap): LuminanceSample {
        val scaled = Bitmap.createScaledBitmap(bitmap, SAMPLE_SIZE, SAMPLE_SIZE, true)
        val pixels = IntArray(SAMPLE_SIZE * SAMPLE_SIZE)
        scaled.getPixels(pixels, 0, SAMPLE_SIZE, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE)

        val luminance = IntArray(pixels.size) { i ->
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }
        return LuminanceSample(luminance, SAMPLE_SIZE)
    }

    private companion object {
        const val SAMPLE_SIZE = 64
    }
}
