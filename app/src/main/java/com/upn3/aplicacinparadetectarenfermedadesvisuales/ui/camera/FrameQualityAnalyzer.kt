package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera

import android.graphics.Bitmap

enum class FrameQuality {
    POSITIONING,
    TOO_DARK,
    TOO_BRIGHT,
    BLURRY,
    GOOD
}

/**
 * Unica responsabilidad: decidir si un frame de camara tiene suficiente luz y nitidez para
 * intentar un analisis (guia de encuadre). No sabe nada de enfermedades ni del modelo TFLite;
 * solo mide brillo promedio y una varianza de laplaciano como heuristica de nitidez sobre una
 * copia reducida del frame (barato de calcular a 1 fps).
 *
 * Los umbrales son heuristicos y pueden necesitar ajuste por dispositivo/camara; priorizan no
 * bloquear la captura por falsos positivos de "borroso" antes que ser estrictos.
 */
class FrameQualityAnalyzer {

    fun analyze(bitmap: Bitmap): FrameQuality {
        val sample = Bitmap.createScaledBitmap(bitmap, SAMPLE_SIZE, SAMPLE_SIZE, true)
        val pixels = IntArray(SAMPLE_SIZE * SAMPLE_SIZE)
        sample.getPixels(pixels, 0, SAMPLE_SIZE, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE)

        val luminance = IntArray(pixels.size) { i ->
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        val brightness = luminance.average()
        return when {
            brightness < DARK_THRESHOLD -> FrameQuality.TOO_DARK
            brightness > BRIGHT_THRESHOLD -> FrameQuality.TOO_BRIGHT
            laplacianVariance(luminance, SAMPLE_SIZE) < SHARPNESS_THRESHOLD -> FrameQuality.BLURRY
            else -> FrameQuality.GOOD
        }
    }

    private fun laplacianVariance(luminance: IntArray, size: Int): Double {
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

    private companion object {
        const val SAMPLE_SIZE = 64
        const val DARK_THRESHOLD = 55.0
        const val BRIGHT_THRESHOLD = 205.0
        const val SHARPNESS_THRESHOLD = 90.0
    }
}
