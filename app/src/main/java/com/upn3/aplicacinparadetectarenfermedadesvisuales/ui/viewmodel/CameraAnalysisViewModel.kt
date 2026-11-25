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

    // Cuantos frames BUENOS seguidos van (se resetea a 0 apenas un frame no es bueno): exigir
    // varios seguidos, en vez de disparar con el primer frame bueno, es lo que evita que un
    // parpadeo o un movimiento breve dispare un analisis sobre un encuadre que en realidad no
    // se sostuvo quieto.
    private var consecutiveGoodFrames = 0

    private val _captureProgress = MutableStateFlow(0f)
    /** 0f..1f: que tan cerca esta de completar los frames buenos seguidos que hacen falta para capturar. */
    val captureProgress: StateFlow<Float> = _captureProgress.asStateFlow()

    // Una vez que se alcanza REQUIRED_GOOD_FRAMES se "congela" la captura (no se vuelve a
    // analizar en cada frame siguiente): el usuario debe pedir explicitamente un nuevo analisis
    // (resetToIdle) para volver a habilitar la deteccion en vivo.
    private val _isCaptureLocked = MutableStateFlow(false)
    val isCaptureLocked: StateFlow<Boolean> = _isCaptureLocked.asStateFlow()

    /**
     * Se llama en cada frame en vivo muestreado por la camara: primero evalua encuadre/luz/
     * nitidez. Si el encuadre es bueno varios frames seguidos (el usuario sostuvo la posicion
     * quieta), recien ahi se dispara el analisis una sola vez y se bloquea hasta el proximo
     * reset; si el encuadre deja de ser bueno en cualquier momento, el conteo se reinicia sin
     * disparar nada (no "jala" un resultado de un encuadre que no se sostuvo).
     */
    fun onFrameCaptured(bitmap: Bitmap, rotationDegrees: Int = 0) {
        if (_isCaptureLocked.value) return
        viewModelScope.launch(Dispatchers.Default) {
            val quality = frameQualityAnalyzer.analyze(bitmap)
            _frameQuality.value = quality
            if (quality == FrameQuality.GOOD) {
                consecutiveGoodFrames = (consecutiveGoodFrames + 1).coerceAtMost(REQUIRED_GOOD_FRAMES)
                _captureProgress.value = consecutiveGoodFrames / REQUIRED_GOOD_FRAMES.toFloat()
                if (consecutiveGoodFrames >= REQUIRED_GOOD_FRAMES) {
                    _isCaptureLocked.value = true
                    analyze(bitmap, rotationDegrees)
                }
            } else {
                consecutiveGoodFrames = 0
                _captureProgress.value = 0f
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
        _captureProgress.value = 0f
        _isCaptureLocked.value = false
        consecutiveGoodFrames = 0
    }

    override fun onCleared() {
        classifier.close()
    }

    private companion object {
        /** A ~1 frame/seg muestreado (ver CameraPreview), 3 equivale a ~3 segundos sostenidos. */
        const val REQUIRED_GOOD_FRAMES = 3
    }
}
