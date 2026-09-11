package com.upn3.aplicacinparadetectarenfermedadesvisuales.domain

enum class RiskLevel {
    BAJO_RIESGO,
    RIESGO_MODERADO,
    SOSPECHA_ALTA
}

/**
 * Solo lleva el codigo de clase (ej. "C" para catarata) y el resultado clinico; el nombre visible
 * se resuelve en la UI a partir de [DiseaseCatalog] segun el idioma activo, para que cambiar el
 * idioma no requiera volver a correr la inferencia.
 */
data class DiagnosisResult(
    val code: String,
    val confidence: Float,
    val riskLevel: RiskLevel
)

/**
 * Umbrales clinicos usados para traducir una probabilidad cruda del modelo en un nivel de riesgo.
 * Vive separado de [MedicalDiagnosticInterpreter] para que ajustar un umbral (ej. 0.75 -> 0.80
 * para sospecha de catarata segun nueva literatura medica) sea un cambio de datos, no de logica.
 */
data class DiagnosticThresholds(
    val highSuspicion: Float = 0.75f,
    val moderate: Float = 0.40f
)

/**
 * Unica razon para cambiar: la politica clinica de interpretacion (umbrales, criterios de riesgo).
 * No sabe nada de TensorFlow Lite, ByteBuffers, cámaras ni idiomas de la UI; solo traduce numeros
 * crudos en un estado de diagnostico comprensible.
 */
class MedicalDiagnosticInterpreter(
    private val thresholds: DiagnosticThresholds = DiagnosticThresholds()
) {

    fun interpret(rawProbabilities: Map<String, Float>): List<DiagnosisResult> {
        return rawProbabilities.mapNotNull { (code, confidence) ->
            if (DiseaseCatalog.byCode(code) == null) return@mapNotNull null
            DiagnosisResult(
                code = code,
                confidence = confidence,
                riskLevel = riskLevelFor(confidence)
            )
        }.sortedByDescending { it.confidence }
    }

    private fun riskLevelFor(confidence: Float): RiskLevel = when {
        confidence >= thresholds.highSuspicion -> RiskLevel.SOSPECHA_ALTA
        confidence >= thresholds.moderate -> RiskLevel.RIESGO_MODERADO
        else -> RiskLevel.BAJO_RIESGO
    }
}
