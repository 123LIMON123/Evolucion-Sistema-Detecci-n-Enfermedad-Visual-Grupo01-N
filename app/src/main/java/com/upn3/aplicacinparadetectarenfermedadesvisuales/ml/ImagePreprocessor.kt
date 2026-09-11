package com.upn3.aplicacinparadetectarenfermedadesvisuales.ml

import android.graphics.Bitmap
import android.graphics.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Estrategia de normalizacion de pixeles. Distintos modelos esperan distintos rangos de entrada;
 * quien conoce cual usar es quien construye el [ImagePreprocessor] (capa de inferencia / composicion),
 * no el preprocesador en si.
 */
enum class PixelNormalization {
    RAW_0_255,
    ZERO_TO_ONE,
    MINUS_ONE_TO_ONE
}

data class ImagePreprocessingSpec(
    val targetWidth: Int,
    val targetHeight: Int,
    val normalization: PixelNormalization
)

/**
 * Unica razon para cambiar: el formato/hardware de entrada (tamano de imagen, tipo de normalizacion
 * de pixeles, correccion de rotacion). No conoce el modelo de TFLite ni las enfermedades que se
 * analizan; solo transforma un [Bitmap] en un [ByteBuffer] Float32 listo para inferencia.
 */
class ImagePreprocessor(private val spec: ImagePreprocessingSpec) {

    fun preprocess(bitmap: Bitmap, rotationDegrees: Int = 0): ByteBuffer {
        val rotated = rotateIfNeeded(bitmap, rotationDegrees)
        val resized = Bitmap.createScaledBitmap(rotated, spec.targetWidth, spec.targetHeight, true)
        return toByteBuffer(resized)
    }

    private fun rotateIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun toByteBuffer(bitmap: Bitmap): ByteBuffer {
        val pixelCount = spec.targetWidth * spec.targetHeight
        val buffer = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * pixelCount * CHANNELS)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(pixelCount)
        bitmap.getPixels(pixels, 0, spec.targetWidth, 0, 0, spec.targetWidth, spec.targetHeight)

        for (pixel in pixels) {
            val (r, g, b) = normalize(pixel)
            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }
        buffer.rewind()
        return buffer
    }

    private fun normalize(pixel: Int): Triple<Float, Float, Float> {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return when (spec.normalization) {
            PixelNormalization.RAW_0_255 -> Triple(r.toFloat(), g.toFloat(), b.toFloat())
            PixelNormalization.ZERO_TO_ONE -> Triple(r / 255f, g / 255f, b / 255f)
            PixelNormalization.MINUS_ONE_TO_ONE -> Triple(
                (r / 127.5f) - 1f,
                (g / 127.5f) - 1f,
                (b / 127.5f) - 1f
            )
        }
    }

    private companion object {
        const val CHANNELS = 3
        const val BYTES_PER_FLOAT = 4
    }
}
