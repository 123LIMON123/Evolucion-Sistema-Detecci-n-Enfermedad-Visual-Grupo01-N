package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.DiagnosisResult
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.MedicalDiagnosticInterpreter
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.EyeDiseaseClassifier
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.ImagePreprocessor
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera.FrameQuality
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera.FrameQualityAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data object Analyzing : AnalysisUiState
    data class Success(val results: List<DiagnosisResult>) : AnalysisUiState
    data class Failed(val message: String) : AnalysisUiState
}

/**
 * Unica razon para cambiar: la logica de la UI/flujo de pantalla (que hacer con cada estado,
 * cuando disparar un analisis). Coordina las capas anteriores solo a traves de sus interfaces
 * (inyectadas por constructor), sin conocer detalles de TFLite ni de reglas clinicas.
 */
class CameraAnalysisViewModel(
    private val imagePreprocessor: ImagePreprocessor,
    private val classifier: EyeDiseaseClassifier,
    private val interpreter: MedicalDiagnosticInterpreter,
    private val frameQualityAnalyzer: FrameQualityAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val _frameQuality = MutableStateFlow(FrameQuality.POSITIONING)
    val frameQuality: StateFlow<FrameQuality> = _frameQuality.asStateFlow()

    /**
     * Se llama en cada frame en vivo muestreado por la camara: primero evalua encuadre/luz/
     * nitidez, y solo dispara el analisis de enfermedad cuando el frame es lo bastante bueno
     * (evita correr inferencia -y mostrar un resultado potencialmente erroneo- sobre una imagen
     * oscura, con glare o borrosa).
     */
    fun onFrameCaptured(bitmap: Bitmap, rotationDegrees: Int = 0) {
        viewModelScope.launch(Dispatchers.Default) {
            val quality = frameQualityAnalyzer.analyze(bitmap)
            _frameQuality.value = quality
            if (quality == FrameQuality.GOOD) {
                analyze(bitmap, rotationDegrees)
            }
        }
    }

    fun analyze(bitmap: Bitmap, rotationDegrees: Int = 0) {
        if (_uiState.value is AnalysisUiState.Analyzing) return

        _uiState.value = AnalysisUiState.Analyzing
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = try {
                val input = imagePreprocessor.preprocess(bitmap, rotationDegrees)
                val rawProbabilities = classifier.classify(input)
                AnalysisUiState.Success(interpreter.interpret(rawProbabilities))
            } catch (e: Exception) {
                AnalysisUiState.Failed(e.message ?: "No se pudo analizar la imagen")
            }
        }
    }

    fun resetToIdle() {
        _uiState.value = AnalysisUiState.Idle
        _frameQuality.value = FrameQuality.POSITIONING
    }

    override fun onCleared() {
        classifier.close()
    }
}
