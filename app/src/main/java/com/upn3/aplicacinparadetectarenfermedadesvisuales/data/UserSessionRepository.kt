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
 * `AuthViewModel`. Ninguna de ellas instancia [InMemoryUserSessionRepository] directamente ni
 * sabe que la sesion vive en memoria; si mañana se guarda en DataStore para sobrevivir un reinicio
 * de la app, se escribe una implementacion nueva y solo se cambia una linea en `AppContainer`.
 */
interface UserSessionRepository {
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
    }
}
