package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiagnosisResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [Principio D - DIP] Abstraccion de la que dependen las pantallas (Camera, History). Ninguna
 * conoce [InMemoryAnalysisHistoryRepository]; si mañana el historial se persiste en Room, se
 * escribe una implementacion nueva y solo se cambia una linea en `AppContainer`.
 */
interface AnalysisHistoryRepository {
    val history: StateFlow<List<DiagnosisResult>>
    fun record(result: DiagnosisResult)
}

/**
 * [Principio S - SRP] Unica razon para cambiar: como se guarda/expone el historial de analisis.
 * Reemplaza la lista global mutable que cualquier pantalla podia tocar directamente.
 *
 * Implementacion de detalle (bajo nivel): hoy en memoria, se pierde al cerrar la app.
 */
class InMemoryAnalysisHistoryRepository : AnalysisHistoryRepository {

    private val _history = MutableStateFlow<List<DiagnosisResult>>(emptyList())
    override val history: StateFlow<List<DiagnosisResult>> = _history.asStateFlow()

    override fun record(result: DiagnosisResult) {
        _history.value = _history.value + result
    }
}
