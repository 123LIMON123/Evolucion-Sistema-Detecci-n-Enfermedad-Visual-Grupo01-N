package com.upn3.aplicacinparadetectarenfermedadesvisuales.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.UserSessionRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.domain.MedicalDiagnosticInterpreter
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.EyeDiseaseClassifier
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.ImagePreprocessingSpec
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.ImagePreprocessor
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.PixelNormalization
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ml.TFLiteDiseaseClassifier
import com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.CameraAnalysisViewModel

/**
 * Composition root manual (sin Hilt/Koin en el proyecto): unico lugar que conoce como se conectan
 * las implementaciones concretas de cada capa. Nadie mas en la app instancia TFLiteDiseaseClassifier
 * ni conoce el nombre del asset del modelo.
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
            normalization = PixelNormalization.RAW_0_255
        )
    )

    val classifier: EyeDiseaseClassifier = TFLiteDiseaseClassifier(
        context = context.applicationContext,
        modelAssetName = "ocular_disease_model.tflite",
        labelCodes = modelLabelOrder
    )

    val diagnosticInterpreter: MedicalDiagnosticInterpreter = MedicalDiagnosticInterpreter()

    val historyRepository: AnalysisHistoryRepository = AnalysisHistoryRepository()

    val userSessionRepository: UserSessionRepository = UserSessionRepository()

    val localizationRepository: LocalizationRepository = LocalizationRepository()
}

class CameraAnalysisViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CameraAnalysisViewModel(
            imagePreprocessor = appContainer.imagePreprocessor,
            classifier = appContainer.classifier,
            interpreter = appContainer.diagnosticInterpreter
        ) as T
    }
}
