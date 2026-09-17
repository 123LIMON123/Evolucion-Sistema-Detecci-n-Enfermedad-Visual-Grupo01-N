package com.upn3.aplicacinparadetectarenfermedadesvisuales.ml

import android.graphics.Bitmap
import android.graphics.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * [Principio O - OCP] Estrategia de normalizacion de pixeles. Antes esto era un `enum` con un
 * `when` adentro de [ImagePreprocessor]: para sumar un rango de normalizacion nuevo habia que
 * editar el enum Y el `when`. Como interfaz, un modelo nuevo con un rango de entrada distinto
 * (ej. [-128, 127] para un modelo cuantizado int8) se soporta escribiendo una clase nueva, sin
 * tocar [ImagePreprocessor] ni las estrategias existentes.
 */
fun interface PixelNormalizationStrategy {
    fun normalize(r: Int, g: Int, b: Int): Triple<Float, Float, Float>
}

/** Pixeles tal cual [0, 255]. Para modelos que ya incluyen su propio `preprocess_input` en el grafo. */
object Raw0To255Normalization : PixelNormalizationStrategy {
    override fun normalize(r: Int, g: Int, b: Int) = Triple(r.toFloat(), g.toFloat(), b.toFloat())
}

/** Pixeles escalados a [0, 1]. */
object ZeroToOneNormalization : PixelNormalizationStrategy {
    override fun normalize(r: Int, g: Int, b: Int) = Triple(r / 255f, g / 255f, b / 255f)
}

/** Pixeles centrados en [-1, 1] (comun en redes tipo MobileNet entrenadas "desde cero"). */
object MinusOneToOneNormalization : PixelNormalizationStrategy {
    override fun normalize(r: Int, g: Int, b: Int) = Triple(
        (r / 127.5f) - 1f,
        (g / 127.5f) - 1f,
        (b / 127.5f) - 1f
    )
}

data class ImagePreprocessingSpec(
    val targetWidth: Int,
    val targetHeight: Int,
    val normalization: PixelNormalizationStrategy
)

/**
 * [Principio S - SRP] Unica razon para cambiar: el formato/hardware de entrada (tamano de imagen,
 * tipo de normalizacion de pixeles, correccion de rotacion). No conoce el modelo de TFLite ni las
 * enfermedades que se analizan; solo transforma un [Bitmap] en un [ByteBuffer] Float32 listo para
 * inferencia.
 *
 * [Principio O - OCP] No necesita conocer todas las estrategias de normalizacion posibles: recibe
 * una via [ImagePreprocessingSpec] y solo la ejecuta. Queda cerrada a modificacion aunque el
 * catalogo de estrategias siga creciendo.
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
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val (nr, ng, nb) = spec.normalization.normalize(r, g, b)
            buffer.putFloat(nr)
            buffer.putFloat(ng)
            buffer.putFloat(nb)
        }
        buffer.rewind()
        return buffer
    }

    private companion object {
        const val CHANNELS = 3
        const val BYTES_PER_FLOAT = 4
    }
}
