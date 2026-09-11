package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiagnosisResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unica razon para cambiar: como se guarda/expone el historial de analisis (hoy en memoria,
 * manana podria ser Room/DataStore). Reemplaza la lista global mutable que cualquier pantalla
 * podia tocar directamente.
 */
class AnalysisHistoryRepository {

    private val _history = MutableStateFlow<List<DiagnosisResult>>(emptyList())
    val history: StateFlow<List<DiagnosisResult>> = _history.asStateFlow()

    fun record(result: DiagnosisResult) {
        _history.value = _history.value + result
    }
}
