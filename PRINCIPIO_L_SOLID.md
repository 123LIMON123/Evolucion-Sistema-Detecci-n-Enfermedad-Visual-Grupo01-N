# Principio L (Sustitución de Liskov) en OcuCheck AI

## ¿Qué dice el principio?

**Un objeto de una clase hija (o de otra implementación de una interfaz) debe poder reemplazar a un objeto de la clase padre sin romper el programa.**

En criollo: si `ProfileScreen` recibe un `UserSessionRepository` y llama a `logout()`, el resultado **tiene que ser el mismo** sin importar si por dentro es `InMemoryUserSessionRepository` o `DataStoreUserSessionRepository`. Si una implementación hace algo distinto a lo que las demás hacen para el mismo método, el código que las usa deja de ser confiable: funciona con una implementación y falla (silenciosamente) con la otra.

El síntoma de que **no** se cumple es exactamente ese: dos clases que implementan la misma interfaz, pero se comportan distinto ante la misma llamada.

---

## Caso real: `logout()` en las dos implementaciones de `UserSessionRepository`

Esto no es un ejemplo armado para la expo — es un bug real que se encontró y se corrigió en este proyecto (commit `4cda611`, revisión de código).

Cuando se agregó `DataStoreUserSessionRepository` (guarda la sesión en disco, para que sobreviva a cerrar la app), su `logout()` se escribió para borrar **todo**: la bandera de sesión Y el nombre/correo guardados. Tenía sentido: si no se borra el nombre/correo, quedan legibles en el archivo de preferencias del teléfono después de que el usuario cerró sesión.

Pero `InMemoryUserSessionRepository` (la implementación más vieja, usada en pruebas) solo borraba la bandera:

### ❌ Antes (violaba LSP)

```kotlin
// InMemoryUserSessionRepository.kt
override fun logout() {
    _isLoggedIn.value = false
    // _currentUser no se toca: el nombre y el correo quedan
    // guardados en memoria aunque la sesion ya este cerrada
}
```

```kotlin
// DataStoreUserSessionRepository.kt (la otra implementacion)
override fun logout() {
    writeSession {
        it.remove(SessionKeys.IS_LOGGED_IN)
        it.remove(SessionKeys.NAME)
        it.remove(SessionKeys.EMAIL)  // esta si borra todo
    }
}
```

**El problema:** cualquier pantalla que dependa de `UserSessionRepository` (como `ProfileScreen`) y llame a `logout()` obtiene un resultado distinto segun cual de las dos implementaciones le haya tocado en suerte. Una prueba escrita contra `InMemoryUserSessionRepository` (mas rapida, no necesita DataStore) podria pasar perfecto y aun asi esconder que la version real en producción deja datos sin limpiar — o al reves, cualquiera podria asumir "los dos limpian el perfil" y estar equivocado para una de las dos.

### ✅ Después (cumple LSP)

```kotlin
// InMemoryUserSessionRepository.kt
override fun logout() {
    _isLoggedIn.value = false
    _currentUser.value = UserProfile(name = "", email = "")
}
```

Ahora **ambas** implementaciones garantizan lo mismo después de `logout()`: `isLoggedIn.value == false` y `currentUser.value == UserProfile("", "")`. Da igual cual este inyectada — el comportamiento observable para quien la usa es idéntico. Eso es justamente lo que pide LSP: la interfaz `UserSessionRepository` no solo define *qué metodos* existen, sino tambien *qué garantizan* esos métodos, y toda implementación tiene que honrar esa garantía.

---

## Otro ejemplo, mismo principio: los repositorios `InMemory...`

Los tres repositorios en memoria (`InMemoryAuthRepository`, `InMemoryAnalysisHistoryRepository`, `InMemoryLocalizationRepository`) tambien son sustitutos válidos de sus interfaces: cualquier prueba unitaria de este proyecto (`AuthViewModelTest`, por ejemplo) usa un **doble de prueba** (`FakeAuthRepository`, `FakeUserSessionRepository`) en lugar de la implementación real, y el `AuthViewModel` funciona exactamente igual, sin enterarse del cambio. Eso solo es posible porque todas las implementaciones de una misma interfaz honran el mismo contrato — si no fuera así, las pruebas con dobles no servirían para predecir el comportamiento real.

---

## En resumen

| Interfaz | Implementaciones que deben comportarse igual |
|---|---|
| `UserSessionRepository` | `InMemoryUserSessionRepository`, `DataStoreUserSessionRepository` |
| `AuthRepository` | `InMemoryAuthRepository`, `FakeAuthRepository` (en tests) |
| `OnboardingRepository` | `InMemoryOnboardingRepository`, `DataStoreOnboardingRepository` |

**La regla practica que se siguió después de encontrar el bug:** cuando un metodo de una interfaz tiene una garantia implicita (ej. "logout() deja al usuario sin datos personales guardados"), esa garantia tiene que verificarse en **cada** implementación, no solo en la mas nueva.
