package com.upn3.aplicacinparadetectarenfermedadesvisuales.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.DataStoreOnboardingRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.FirebaseAuthRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.FirebaseUserSessionRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.FirestoreAnalysisHistoryRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.InMemoryLocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.OnboardingRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.DataStoreReminderRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.ReminderRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.DataStoreThemeRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.ThemeRepository
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
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// DataStore para el estado del onboarding (la sesion ahora la maneja FirebaseAuth, ver
// FirebaseUserSessionRepository, asi que ya no hace falta un DataStore propio para eso).
private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding")

// DataStore para la preferencia de tema (modo oscuro), igual criterio que onboardingDataStore:
// no tiene nada que ver con la sesion, va en su propio archivo.
private val Context.themeDataStore by preferencesDataStore(name = "theme")

// DataStore para la preferencia de recordatorios, mismo criterio.
private val Context.reminderDataStore by preferencesDataStore(name = "reminders")

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

    // Alcance de corrutinas de toda la app, para repositorios que escriben a disco en segundo
    // plano (ej. DataStore). Vive tanto como el proceso, igual que este AppContainer (ambos los
    // crea EyeDiseaseApp una unica vez).
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // El modelo se entreno con MobileNetV2 a 160x160 y estas 3 clases, en este orden exacto
    // (ver ml/train.py, LABEL_COLS). Las 3 vienen de fotos de ojo EXTERNO (camara normal del
    // celular, no fondo de ojo): N (normal), C (catarata, visible en estadio avanzado) y CONJ
    // (conjuntivitis). El modelo anterior tenia 9 clases entrenadas en su mayoria con fotos de
    // fondo de ojo (ODIR-5K), un dominio visual que la camara del celular nunca captura; ver
    // ml/EXTERNAL_DATASETS.md para el detalle de por que se redujo a estas 3. Este orden es un
    // detalle de la estructura de tensores del .tflite, por eso vive junto a donde se construye
    // el clasificador concreto.
    private val modelLabelOrder = listOf("N", "C", "CONJ")
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

    // Firebase Authentication + Firestore (proyecto "OcucheckAi") reemplazan las versiones en
    // memoria/DataStore en este unico lugar: ninguna pantalla ni ViewModel se entera del cambio
    // porque todas dependen de las interfaces (AuthRepository, UserSessionRepository,
    // AnalysisHistoryRepository), nunca de estas clases concretas.
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val historyRepository: AnalysisHistoryRepository = FirestoreAnalysisHistoryRepository(
        firestore = firestore,
        firebaseAuth = firebaseAuth,
        externalScope = appScope
    )

    // [Principio D - DIP] FirebaseUserSessionRepository reemplazo a DataStoreUserSessionRepository
    // en este unico lugar para que la sesion viva en la cuenta real de Firebase (sobrevive a
    // cerrar la app y se comparte entre dispositivos); ninguna pantalla ni el AuthViewModel se
    // enteraron del cambio porque dependen de la interfaz UserSessionRepository.
    val userSessionRepository: UserSessionRepository = FirebaseUserSessionRepository(
        firebaseAuth = firebaseAuth,
        firestore = firestore,
        externalScope = appScope
    )

    val authRepository: AuthRepository = FirebaseAuthRepository(
        firebaseAuth = firebaseAuth,
        firestore = firestore
    )

    val localizationRepository: LocalizationRepository = InMemoryLocalizationRepository()

    val onboardingRepository: OnboardingRepository = DataStoreOnboardingRepository(
        dataStore = context.applicationContext.onboardingDataStore,
        externalScope = appScope
    )

    val themeRepository: ThemeRepository = DataStoreThemeRepository(
        dataStore = context.applicationContext.themeDataStore,
        externalScope = appScope
    )

    val reminderRepository: ReminderRepository = DataStoreReminderRepository(
        dataStore = context.applicationContext.reminderDataStore,
        externalScope = appScope
    )
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
