package com.upn3.aplicacinparadetectarenfermedadesvisuales.ml

import java.nio.ByteBuffer

/**
 * [Principio S - SRP] Unica responsabilidad: definir el contrato de inferencia. Recibe un
 * ByteBuffer ya preprocesado y entrega probabilidades crudas por codigo de clase, sin ninguna
 * interpretacion clinica.
 *
 * [Principio O - OCP] Este es el ejemplo mas directo de OCP en el proyecto: para sumar un motor
 * de inferencia nuevo (ONNX Runtime, un servicio en la nube, un modelo de prueba fijo para tests)
 * se escribe una clase que implemente esta interfaz -tal como [TFLiteDiseaseClassifier]- y se
 * cambia UNA linea en `AppContainer`. [CameraAnalysisViewModel] depende solo de esta interfaz,
 * nunca de una implementacion concreta, asi que no hay que tocarlo ni tocar el resto del pipeline.
 */
interface EyeDiseaseClassifier {
    fun classify(input: ByteBuffer): Map<String, Float>
    fun close()
}
