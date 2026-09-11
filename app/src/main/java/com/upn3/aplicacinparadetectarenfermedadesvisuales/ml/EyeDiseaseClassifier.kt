package com.upn3.aplicacinparadetectarenfermedadesvisuales.ml

import java.nio.ByteBuffer

/**
 * Contrato de inferencia. Recibe un ByteBuffer ya preprocesado y entrega probabilidades crudas
 * por codigo de clase, sin ninguna interpretacion clinica. Permite intercambiar el motor de
 * inferencia (TFLite, ONNX Runtime, etc.) sin tocar el resto del pipeline.
 */
interface EyeDiseaseClassifier {
    fun classify(input: ByteBuffer): Map<String, Float>
    fun close()
}
