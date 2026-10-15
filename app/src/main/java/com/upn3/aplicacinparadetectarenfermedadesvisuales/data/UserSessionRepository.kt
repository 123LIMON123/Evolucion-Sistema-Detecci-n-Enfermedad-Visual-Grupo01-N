package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val name: String,
    val email: String
)

/**
 * [Principio D - DIP] Abstraccion de la que dependen las pantallas (Login, Register, Profile) y
 * `AuthViewModel`. Ninguna de ellas sabe si la sesion vive solo en memoria ([InMemoryUserSessionRepository])
 * o persistida en disco (`DataStoreUserSessionRepository`): ese cambio se hizo en un solo lugar
 * (`AppContainer`) sin tocar ninguna pantalla ni el ViewModel.
 */
interface UserSessionRepository {
    /**
     * `true` cuando ya se termino de leer el estado de sesion persistido (o inmediatamente, para
     * una implementacion que no persiste nada). Mientras sea `false`, [isLoggedIn] todavia no es
     * confiable: la pantalla de arranque debe esperar antes de decidir si mostrar Login o Home.
     */
    val isReady: StateFlow<Boolean>
    val isLoggedIn: StateFlow<Boolean>
    val currentUser: StateFlow<UserProfile>
    fun login(profile: UserProfile)
    fun logout()
}

/**
 * [Principio S - SRP] Unica razon para cambiar: como se gestiona la sesion del usuario. Reemplaza
 * las variables globales sueltas (usuarioLogueado, nombreUsuario, emailUsuario) por un estado
 * encapsulado y observable.
 *
 * Implementacion de detalle (bajo nivel): guarda la sesion en memoria mientras el proceso vive.
 */
class InMemoryUserSessionRepository : UserSessionRepository {

    // No hay nada que leer de disco: esta listo apenas se crea.
    override val isReady: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(UserProfile(name = "", email = ""))
    override val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    override fun login(profile: UserProfile) {
        _currentUser.value = profile
        _isLoggedIn.value = true
    }

    override fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = UserProfile(name = "", email = "")
    }
}
