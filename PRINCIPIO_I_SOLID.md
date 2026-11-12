# Principio I (Segregación de Interfaces) en OcuCheck AI

## ¿Qué dice el principio?

**Ningún cliente debería verse obligado a depender de métodos que no usa.**

En criollo: si una pantalla solo necesita "saber si el usuario está logueado", su interfaz no debería obligarla a también cargar con métodos de autenticación, historial o idioma. Mejor **varias interfaces chicas y específicas** que **una sola interfaz gigante** que lo haga todo.

El síntoma de que **no** se cumple es una interfaz con muchos métodos de temas distintos, donde la mayoría de las clases que la implementan o la usan solo necesitan una porción de ella (y el resto queda vacío, con un `TODO()`, o simplemente ignorado).

---

## Cómo se vería SIN este principio (para comparar)

Si en vez de los repositorios actuales hubiera **un solo repositorio gigante** con todo lo que la app necesita guardar:

```kotlin
interface AppRepository {
    // autenticacion
    fun login(email: String, password: String): AuthResult
    fun register(name: String, email: String, password: String, confirm: String): AuthResult

    // sesion
    val isLoggedIn: StateFlow<Boolean>
    val currentUser: StateFlow<UserProfile>
    fun logout()

    // historial de analisis
    val history: StateFlow<List<DiagnosisResult>>
    fun record(result: DiagnosisResult)
    fun clearHistory()

    // idioma
    val language: StateFlow<AppLanguage>
    fun toggleLanguage()
}
```

**El problema:** `ProfileScreen` solo necesita leer `currentUser` y llamar a `logout()`, pero su firma recibiría un `AppRepository` completo — quedaría "obligada" a convivir con métodos de login, historial e idioma que nunca usa. Si mañana cambia algo de `history` (por ejemplo, se agrega paginación), hay que revisar **todas** las pantallas que reciben `AppRepository`, aunque la mayoría ni toque el historial. Y cualquier implementación de prueba (un *fake* para tests) tendría que implementar los 9 métodos, aunque la prueba solo necesite 2.

---

## Cómo está en este proyecto (cumple ISP)

Cada repositorio es su propia interfaz, chica y enfocada en una sola cosa:

| Interfaz | Métodos | Quién la usa |
|---|---|---|
| `AuthRepository` | `login()`, `register()` | Solo `AuthViewModel` |
| `UserSessionRepository` | `isLoggedIn`, `currentUser`, `isReady`, `login()`, `logout()` | `AuthViewModel`, `ProfileScreen`, `AppNavHost` |
| `AnalysisHistoryRepository` | `history`, `record()`, `clear()` | `CameraScreen`, `HistoryScreen`, `HomeScreen`, `SettingsScreen` |
| `LocalizationRepository` | `language` (vía `toggle()`) | Casi todas las pantallas (vía `AppTopBar`) |
| `OnboardingRepository` | `isReady`, `hasCompletedOnboarding`, `markCompleted()` | Solo `AppNavHost` |
| `EyeDiseaseClassifier` | `classify()`, `close()` | Solo `CameraAnalysisViewModel` |

```kotlin
// ProfileScreen solo pide lo que de verdad necesita:
fun ProfileScreen(
    navController: NavController,
    userSession: UserSessionRepository,       // no AuthRepository ni History
    localizationRepository: LocalizationRepository
) { ... }
```

**¿Por qué cumple ISP?** Cada pantalla, ViewModel o prueba recibe **exactamente** las interfaces que va a usar, ni una más. `SettingsScreen` necesita borrar el historial y cambiar el idioma, así que recibe `AnalysisHistoryRepository` + `LocalizationRepository` — nunca `AuthRepository`, porque Ajustes no inicia sesión ni registra usuarios. Un doble de prueba (`FakeUserSessionRepository`, visto en `AuthViewModelTest`) solo tiene que simular 5 métodos chicos, no una interfaz de 15.

---

## En resumen

| Si hubiera una sola interfaz gigante | Con interfaces chicas (como está) |
|---|---|
| Cambiar el historial obliga a revisar pantallas que no lo usan | Cambiar `AnalysisHistoryRepository` solo afecta a quien la recibe |
| Un *fake* de prueba implementa 9+ métodos, use o no todos | Un *fake* implementa 2-5 métodos, los que la prueba necesita |
| Dificil saber, mirando la firma de una pantalla, qué hace de verdad | La firma de cada pantalla ya dice qué datos toca |
