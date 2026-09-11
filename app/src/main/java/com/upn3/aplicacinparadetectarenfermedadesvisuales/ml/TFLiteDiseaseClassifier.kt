package com.upn3.aplicacinparadetectarenfermedadesvisuales.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/**
 * Unica razon para cambiar: la version de la libreria TensorFlow Lite o la estructura de tensores
 * del archivo .tflite (tamano de salida, orden de las clases del modelo). No sabe que significan
 * clinicamente los numeros que devuelve, eso es responsabilidad de la capa de interpretacion.
 *
 * [labelCodes] debe respetar el orden exacto de las columnas de salida con las que se entreno el
 * modelo (ver ml/train.py, LABEL_COLS).
 */
class TFLiteDiseaseClassifier(
    private val context: Context,
    private val modelAssetName: String,
    private val labelCodes: List<String>
) : EyeDiseaseClassifier {

    private var interpreter: Interpreter? = null

    override fun classify(input: ByteBuffer): Map<String, Float> {
        val activeInterpreter = interpreter ?: loadInterpreter().also { interpreter = it }
        val output = Array(1) { FloatArray(labelCodes.size) }
        activeInterpreter.run(input, output)
        return labelCodes.indices.associate { index -> labelCodes[index] to output[0][index] }
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }

    private fun loadInterpreter(): Interpreter {
        context.assets.openFd(modelAssetName).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).use { inputStream ->
                val modelBuffer = inputStream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.startOffset,
                    descriptor.declaredLength
                )
                return Interpreter(modelBuffer)
            }
        }
    }
}
