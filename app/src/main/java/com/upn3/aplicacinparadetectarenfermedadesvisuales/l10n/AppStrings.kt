package com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n

/**
 * Catalogo unico de textos de UI en espanol e ingles. Todas las pantallas leen de aqui (via
 * [LocalAppStrings]) en lugar de tener texto suelto embebido, para que agregar/editar un idioma
 * sea un cambio en un solo archivo.
 */
data class AppStrings(
    // comun
    val back: String,
    val switchToLanguageLabel: String,
    // login
    val loginTitle: String,
    val loginSubtitle: String,
    val loginButton: String,
    val noAccountPrompt: String,
    // register
    val registerTitle: String,
    val registerSubtitle: String,
    val backToLogin: String,
    // home
    val appTitle: String,
    val homeWelcome: String,
    val homeSubtitle: String,
    val newAnalysis: String,
    val newAnalysisDescription: String,
    val historyNav: String,
    val historyDescription: String,
    val diseaseInfoNav: String,
    val diseaseInfoDescription: String,
    val profileNav: String,
    val profileDescription: String,
    // camera
    val cameraTitle: String,
    val analyzingMessage: String,
    val errorPrefix: String,
    val viewDetail: String,
    val uploadPhoto: String,
    val uploadAnotherPhoto: String,
    val backToLiveCamera: String,
    val cameraPermissionRequired: String,
    val uploadPhotoInstead: String,
    // analysis result
    val resultTitle: String,
    val probableDetection: String,
    val confidenceLabel: String,
    val riskLow: String,
    val riskModerate: String,
    val riskHigh: String,
    val newAnalysisAction: String,
    val moreInfoAction: String,
    // disease info
    val diseaseInfoTitle: String,
    val commonSymptoms: String,
    val recommendationLabel: String,
    // history
    val historyTitle: String,
    val noHistoryYet: String,
    // profile
    val profileTitle: String,
    val logout: String
)

val EsStrings = AppStrings(
    back = "Volver",
    switchToLanguageLabel = "Switch to English",
    loginTitle = "Bienvenido de nuevo",
    loginSubtitle = "Inicia sesión para analizar la salud de tus ojos",
    loginButton = "Iniciar sesión",
    noAccountPrompt = "¿No tienes cuenta? Regístrate",
    registerTitle = "Crear cuenta",
    registerSubtitle = "Regístrate para guardar tu historial de análisis",
    backToLogin = "Volver al inicio de sesión",
    appTitle = "Detector de Enfermedades Oculares",
    homeWelcome = "Hola, cuida tu visión",
    homeSubtitle = "Elige una opción para continuar",
    newAnalysis = "Nuevo análisis",
    newAnalysisDescription = "Usa la cámara o sube una foto de un ojo",
    historyNav = "Historial",
    historyDescription = "Revisa tus análisis anteriores",
    diseaseInfoNav = "Enfermedades",
    diseaseInfoDescription = "Aprende sobre cada condición ocular",
    profileNav = "Perfil",
    profileDescription = "Tu cuenta y preferencias",
    cameraTitle = "Analizar ojo",
    analyzingMessage = "Analizando... (asegúrese de tener el modelo en assets)",
    errorPrefix = "Error",
    viewDetail = "Ver detalle",
    uploadPhoto = "Subir foto de un ojo",
    uploadAnotherPhoto = "Subir otra foto",
    backToLiveCamera = "Volver a la cámara en vivo",
    cameraPermissionRequired = "Se requiere permiso de cámara para continuar",
    uploadPhotoInstead = "O subir una foto de un ojo",
    resultTitle = "Resultado del análisis",
    probableDetection = "Detección probable",
    confidenceLabel = "Confianza",
    riskLow = "Bajo riesgo",
    riskModerate = "Riesgo moderado",
    riskHigh = "Sospecha alta",
    newAnalysisAction = "Realizar nuevo análisis",
    moreInfoAction = "Más información sobre esta condición",
    diseaseInfoTitle = "Información de enfermedades",
    commonSymptoms = "Síntomas comunes",
    recommendationLabel = "Recomendación",
    historyTitle = "Historial de análisis",
    noHistoryYet = "Aún no tienes análisis guardados.",
    profileTitle = "Mi perfil",
    logout = "Cerrar sesión"
)

val EnStrings = AppStrings(
    back = "Back",
    switchToLanguageLabel = "Cambiar a español",
    loginTitle = "Welcome back",
    loginSubtitle = "Sign in to analyze your eye health",
    loginButton = "Log in",
    noAccountPrompt = "Don't have an account? Register",
    registerTitle = "Create account",
    registerSubtitle = "Register to save your analysis history",
    backToLogin = "Back to login",
    appTitle = "Eye Disease Detector",
    homeWelcome = "Hi, take care of your vision",
    homeSubtitle = "Choose an option to continue",
    newAnalysis = "New analysis",
    newAnalysisDescription = "Use the camera or upload a photo of an eye",
    historyNav = "History",
    historyDescription = "Review your previous analyses",
    diseaseInfoNav = "Diseases",
    diseaseInfoDescription = "Learn about each eye condition",
    profileNav = "Profile",
    profileDescription = "Your account and preferences",
    cameraTitle = "Analyze eye",
    analyzingMessage = "Analyzing... (make sure the model is in assets)",
    errorPrefix = "Error",
    viewDetail = "View detail",
    uploadPhoto = "Upload a photo of an eye",
    uploadAnotherPhoto = "Upload another photo",
    backToLiveCamera = "Back to live camera",
    cameraPermissionRequired = "Camera permission is required to continue",
    uploadPhotoInstead = "Or upload a photo of an eye",
    resultTitle = "Analysis result",
    probableDetection = "Probable detection",
    confidenceLabel = "Confidence",
    riskLow = "Low risk",
    riskModerate = "Moderate risk",
    riskHigh = "High suspicion",
    newAnalysisAction = "Run a new analysis",
    moreInfoAction = "More information about this condition",
    diseaseInfoTitle = "Disease information",
    commonSymptoms = "Common symptoms",
    recommendationLabel = "Recommendation",
    historyTitle = "Analysis history",
    noHistoryYet = "You don't have any saved analyses yet.",
    profileTitle = "My profile",
    logout = "Log out"
)

fun AppLanguage.strings(): AppStrings = when (this) {
    AppLanguage.ES -> EsStrings
    AppLanguage.EN -> EnStrings
}
