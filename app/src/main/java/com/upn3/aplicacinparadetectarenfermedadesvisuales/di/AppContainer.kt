package com.upn3.aplicacinparadetectarenfermedadesvisuales.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.InMemoryAnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.InMemoryAuthRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.InMemoryLocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.InMemoryUserSessionRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.UserSessionRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.MedicalDiagnosticInterpreter
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.EyeDiseaseClassifier
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.ImagePreprocessingSpec
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.ImagePreprocessor
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.Raw0To255Normalization
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.TFLiteDiseaseClassifier
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.camera.FrameQualityAnalyzer
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.AuthViewModel
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.CameraAnalysisViewModel

/**
 * [Principio S - SRP] Unica responsabilidad: cablear (instanciar y conectar) las implementaciones
 * concretas de cada capa. Composition root manual (sin Hilt/Koin en el proyecto): unico lugar que
 * conoce como se conectan las implementaciones concretas de cada capa. Nadie mas en la app
 * instancia TFLiteDiseaseClassifier ni conoce el nombre del asset del modelo.
 *
 * [Principio D - DIP] Es el UNICO lugar del proyecto que conoce los nombres concretos
 * (`InMemoryAuthRepository`, `InMemoryUserSessionRepository`, `TFLiteDiseaseClassifier`, etc.).
 * Todo lo demas (ViewModels, pantallas) recibe sus dependencias tipadas por interfaz. Cambiar una
 * implementacion de bajo nivel es cambiar una linea aca, nunca en el codigo que la usa.
 */
class AppContainer(context: Context) {

    // El modelo se entreno con MobileNetV2 a 160x160 y estas 8 clases, en este orden exacto
    // (ver ml/train.py, LABEL_COLS). Este orden es un detalle de la estructura de tensores del
    // .tflite, por eso vive junto a donde se construye el clasificador concreto.
    private val modelLabelOrder = listOf("N", "D", "G", "C", "A", "H", "M", "O")
    private val MODEL_INPUT_SIZE = 160

    val imagePreprocessor: ImagePreprocessor = ImagePreprocessor(
        ImagePreprocessingSpec(
            targetWidth = MODEL_INPUT_SIZE,
            targetHeight = MODEL_INPUT_SIZE,
            // El modelo trae el preprocess_input de MobileNetV2 horneado en el grafo, por lo que
            // espera pixeles crudos [0, 255]. Normalizar aca tambien normalizaria dos veces.
            normalization = Raw0To255Normalization
        )
    )

    val classifier: EyeDiseaseClassifier = TFLiteDiseaseClassifier(
        context = context.applicationContext,
        modelAssetName = "ocular_disease_model.tflite",
        labelCodes = modelLabelOrder
    )

    val diagnosticInterpreter: MedicalDiagnosticInterpreter = MedicalDiagnosticInterpreter()

    // [Principio O - OCP] Usa la lista de reglas por defecto (oscuro/brillo/borroso). Para sumar
    // un chequeo nuevo (ej. deteccion de glare) se le pasa una lista con esa regla agregada aca,
    // sin tocar FrameQualityAnalyzer.
    val frameQualityAnalyzer: FrameQualityAnalyzer = FrameQualityAnalyzer()

    val historyRepository: AnalysisHistoryRepository = InMemoryAnalysisHistoryRepository()

    val userSessionRepository: UserSessionRepository = InMemoryUserSessionRepository()

    val authRepository: AuthRepository = InMemoryAuthRepository()

    val localizationRepository: LocalizationRepository = InMemoryLocalizationRepository()
}

/**
 * [Principio S - SRP] Unica responsabilidad: saber como construir un [CameraAnalysisViewModel]
 * con sus dependencias del [AppContainer]. Es lo unico en la app que llama a su constructor.
 */
class CameraAnalysisViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CameraAnalysisViewModel(
            imagePreprocessor = appContainer.imagePreprocessor,
            classifier = appContainer.classifier,
            interpreter = appContainer.diagnosticInterpreter,
            frameQualityAnalyzer = appContainer.frameQualityAnalyzer
        ) as T
    }
}

/**
 * [Principio S - SRP] Unica responsabilidad: saber como construir un [AuthViewModel] con sus
 * dependencias del [AppContainer].
 */
class AuthViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(
            authRepository = appContainer.authRepository,
            userSessionRepository = appContainer.userSessionRepository
        ) as T
    }
}
