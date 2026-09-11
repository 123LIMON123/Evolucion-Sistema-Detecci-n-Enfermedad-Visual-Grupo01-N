package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val name: String,
    val email: String
)

/**
 * Unica razon para cambiar: como se gestiona la sesion del usuario. Reemplaza las variables
 * globales sueltas (usuarioLogueado, nombreUsuario, emailUsuario) por un estado encapsulado y
 * observable.
 */
class UserSessionRepository {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(UserProfile(name = "Usuario de Prueba", email = "usuario@ejemplo.com"))
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    fun login() {
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}
