# Principio D (Inversión de Dependencia) en OcuCheck AI

## ¿Qué dice el principio?

**Los módulos de alto nivel no deben depender de módulos de bajo nivel. Ambos deben depender de abstracciones.**

En criollo: la pantalla (alto nivel, "qué hace la app") **no debería saber** cómo está construida por dentro una clase concreta (bajo nivel, "cómo lo hace"). Las dos deberían hablar a través de una **interfaz**. Así, cambiar el "cómo" (ej. pasar de guardar en memoria a guardar en una base de datos) no obliga a tocar el "qué".

El síntoma de que **no** se cumple es que una pantalla o ViewModel escribe `= NombreDeClaseConcreta()` o recibe el tipo concreto en su firma, en vez de una interfaz.

Esta expo, junto con el aprovechamiento para crear el login real, muestra los cambios hechos.

---

## 1. Los 3 repositorios existentes: de clase concreta a interfaz + implementación

### ❌ Antes (violaba DIP)

Las pantallas recibían **la clase concreta** directamente. `HomeScreen`, `CameraScreen`, `ProfileScreen`, etc. dependían de `UserSessionRepository`, `AnalysisHistoryRepository` y `LocalizationRepository` como si fueran la única forma posible de guardar esos datos.

```kotlin
// data/UserSessionRepository.kt (antes)
class UserSessionRepository {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    fun login() { _isLoggedIn.value = true }
    fun logout() { _isLoggedIn.value = false }
}

// ProfileScreen.kt (antes)
fun ProfileScreen(navController: NavController, userSession: UserSessionRepository) { ... }
```

Si mañana se quisiera guardar la sesión en `DataStore` para que sobreviva a cerrar la app, habría que **crear una clase nueva Y cambiar la firma de cada pantalla** que la usa.

### ✅ Después (cumple DIP)

Cada repositorio ahora es una **interfaz** (el contrato) + una clase `InMemory...` (el detalle de implementación, bajo nivel).

| Interfaz (contrato) | Implementación actual (detalle) |
|---|---|
| `UserSessionRepository` | `InMemoryUserSessionRepository` |
| `AnalysisHistoryRepository` | `InMemoryAnalysisHistoryRepository` |
| `LocalizationRepository` | `InMemoryLocalizationRepository` |

```kotlin
// data/UserSessionRepository.kt (después)
interface UserSessionRepository {
    val isLoggedIn: StateFlow<Boolean>
    val currentUser: StateFlow<UserProfile>
    fun login(profile: UserProfile)
    fun logout()
}

class InMemoryUserSessionRepository : UserSessionRepository { ... }
```

**Lo más importante:** como la interfaz se quedó con el mismo nombre que tenía la clase antes, **ninguna pantalla tuvo que cambiar su firma** (`ProfileScreen(userSession: UserSessionRepository)` sigue compilando igual). Solo cambió `AppContainer`, que ahora es el único lugar que menciona `InMemory...`:

```kotlin
// di/AppContainer.kt (después)
val userSessionRepository: UserSessionRepository = InMemoryUserSessionRepository()
val historyRepository: AnalysisHistoryRepository = InMemoryAnalysisHistoryRepository()
val localizationRepository: LocalizationRepository = InMemoryLocalizationRepository()
```

Si mañana se quiere guardar la sesión en `DataStore`: se escribe `DataStoreUserSessionRepository : UserSessionRepository` y se cambia **una sola línea** de `AppContainer`. Ninguna pantalla se entera.

---

## 2. Login real: `AuthRepository` + `AuthViewModel` (ejemplo nuevo, de punta a punta)

Antes, "iniciar sesión" era solo apretar un botón — no existía ninguna validación real. Se aprovechó para construir el login real aplicando DIP desde el diseño:

### Piezas nuevas

| Archivo | Nivel | Qué es |
|---|---|---|
| `AuthRepository` (interfaz) | Abstracción | El contrato: `login(email, password)` y `register(...)`, sin decir cómo se valida. |
| `InMemoryAuthRepository` | Bajo nivel (detalle) | Guarda las cuentas en un mapa en memoria y valida contra eso. Incluye una cuenta demo (`demo@ocucheck.ai` / `demo1234`) para poder probar sin registrarse. |
| `AuthViewModel` | Alto nivel | Coordina el flujo de login/registro. **Depende de las interfaces `AuthRepository` y `UserSessionRepository`, nunca de `InMemoryAuthRepository`.** |
| `LoginScreen` / `RegisterScreen` | Alto nivel (UI) | Ahora tienen campos reales (correo, contraseña, nombre, confirmar contraseña) y muestran los errores que devuelve el ViewModel. Dependen de `AuthViewModel`, no de un repositorio concreto. |

### ❌ Cómo se vería SIN DIP (para comparar)

```kotlin
class AuthViewModel {
    private val authRepository = InMemoryAuthRepository() // clase concreta "hardcodeada"

    fun login(email: String, password: String) {
        authRepository.login(email, password) // atado para siempre a la memoria
    }
}
```

Si mañana el login tuviera que hablar con Firebase Auth o una API propia, habría que **reescribir `AuthViewModel`**.

### ✅ Cómo quedó (con DIP)

```kotlin
class AuthViewModel(
    private val authRepository: AuthRepository,        // abstraccion
    private val userSessionRepository: UserSessionRepository // abstraccion
) : ViewModel() {

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        when (val result = authRepository.login(email, password)) {
            is AuthResult.Success -> {
                userSessionRepository.login(result.profile)
                onSuccess()
            }
            is AuthResult.Failure -> { /* mostrar error */ }
        }
    }
}
```

`AuthViewModel` **nunca escribe `InMemoryAuthRepository`** en su código. Quien decide cuál usar es `AppContainer` (el único lugar que conoce el detalle concreto), y se lo pasa ya armado a través de `AuthViewModelFactory`:

```kotlin
// di/AppContainer.kt
val authRepository: AuthRepository = InMemoryAuthRepository()

class AuthViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(
            authRepository = appContainer.authRepository,
            userSessionRepository = appContainer.userSessionRepository
        ) as T
    }
}
```

Si mañana se quiere validar contra un backend real, se escribe `FirebaseAuthRepository : AuthRepository` y se cambia una línea en `AppContainer`. **`AuthViewModel`, `LoginScreen` y `RegisterScreen` quedan exactamente iguales.**

---

## Resumen de archivos tocados

| Archivo | Cambio |
|---|---|
| `data/UserSessionRepository.kt` | Se separó en interfaz + `InMemoryUserSessionRepository`. `login()` ahora recibe el perfil real (`login(profile: UserProfile)`). |
| `data/AnalysisHistoryRepository.kt` | Se separó en interfaz + `InMemoryAnalysisHistoryRepository`. |
| `data/LocalizationRepository.kt` | Se separó en interfaz + `InMemoryLocalizationRepository`. |
| `data/AuthRepository.kt` (nuevo) | Interfaz `AuthRepository` + `InMemoryAuthRepository` con validación real y cuenta demo. |
| `ui/viewmodel/AuthViewModel.kt` (nuevo) | Coordina login/registro, depende solo de interfaces. |
| `ui/screens/LoginScreen.kt` | Ahora tiene campos de correo/contraseña reales, muestra carga y errores. |
| `ui/screens/RegisterScreen.kt` | Ahora tiene campos de nombre/correo/contraseña/confirmar, muestra carga y errores. |
| `di/AppContainer.kt` | Único lugar que menciona las clases `InMemory...`; agrega `authRepository` y `AuthViewModelFactory`. |
| `navigation/AppNavHost.kt` | Crea el `AuthViewModel` y se lo pasa a Login/Register en vez de pasarles el repositorio de sesión directo. |

## Para probar

En el login ya funcional, usar la cuenta demo: **demo@ocucheck.ai / demo1234**, o crear una cuenta nueva desde "Registrarse".
