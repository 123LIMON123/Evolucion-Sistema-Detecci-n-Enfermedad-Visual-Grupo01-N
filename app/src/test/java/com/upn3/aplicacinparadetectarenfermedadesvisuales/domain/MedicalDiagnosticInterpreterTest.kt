package com.upn3.aplicacinparadetectarenfermedadesvisuales.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba la unica regla que de verdad importa en este modulo: que umbral clinico produce que
 * nivel de riesgo. Usa los umbrales por defecto (0.75 alta sospecha, 0.40 moderado).
 */
class MedicalDiagnosticInterpreterTest {

    private val interpreter = MedicalDiagnosticInterpreter()

    @Test
    fun `confidence below the moderate threshold is low risk`() {
        val results = interpreter.interpret(mapOf("N" to 0.10f))
        assertEquals(RiskLevel.BAJO_RIESGO, results.single().riskLevel)
    }

    @Test
    fun `confidence at the moderate threshold is moderate risk`() {
        val results = interpreter.interpret(mapOf("C" to 0.40f))
        assertEquals(RiskLevel.RIESGO_MODERADO, results.single().riskLevel)
    }

    @Test
    fun `confidence just below the high suspicion threshold is still moderate`() {
        val results = interpreter.interpret(mapOf("C" to 0.749f))
        assertEquals(RiskLevel.RIESGO_MODERADO, results.single().riskLevel)
    }

    @Test
    fun `confidence at the high suspicion threshold is high suspicion`() {
        val results = interpreter.interpret(mapOf("G" to 0.75f))
        assertEquals(RiskLevel.SOSPECHA_ALTA, results.single().riskLevel)
    }

    @Test
    fun `custom thresholds are respected`() {
        val lenient = MedicalDiagnosticInterpreter(DiagnosticThresholds(highSuspicion = 0.90f, moderate = 0.60f))
        val results = lenient.interpret(mapOf("G" to 0.80f))
        assertEquals(RiskLevel.RIESGO_MODERADO, results.single().riskLevel)
    }

    @Test
    fun `results are sorted by confidence descending`() {
        val results = interpreter.interpret(mapOf("N" to 0.10f, "D" to 0.80f, "C" to 0.45f))
        assertEquals(listOf("D", "C", "N"), results.map { it.code })
    }

    @Test
    fun `unknown disease codes are dropped, not crashed on`() {
        val results = interpreter.interpret(mapOf("N" to 0.5f, "ZZ" to 0.9f))
        assertEquals(1, results.size)
        assertEquals("N", results.single().code)
    }

    @Test
    fun `empty input produces empty output`() {
        assertTrue(interpreter.interpret(emptyMap()).isEmpty())
    }

    @Test
    fun `every result keeps the raw confidence it was given`() {
        val results = interpreter.interpret(mapOf("M" to 0.6234f))
        assertEquals(0.6234f, results.single().confidence, 0.0001f)
    }

    @Test
    fun `an unknown-only map never throws`() {
        assertNull(interpreter.interpret(mapOf("??" to 1f)).firstOrNull())
    }
}
