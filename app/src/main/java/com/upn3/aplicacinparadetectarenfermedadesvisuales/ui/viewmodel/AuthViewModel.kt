package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthFailureReason
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthResult
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val reason: AuthFailureReason) : AuthUiState
}

/**
 * [Principio S - SRP] Unica responsabilidad: coordinar el flujo de login/registro (que hacer con
 * cada resultado, cuando marcar la sesion como iniciada). No valida credenciales ni sabe donde se
 * guardan: eso se lo pide a [AuthRepository] y [UserSessionRepository].
 *
 * [Principio D - DIP] Es el ejemplo mas directo de DIP del proyecto: este ViewModel (modulo de
 * ALTO nivel, decide el flujo de la pantalla) depende de dos ABSTRACCIONES (`AuthRepository`,
 * `UserSessionRepository`), nunca de `InMemoryAuthRepository` ni `InMemoryUserSessionRepository`
 * (modulos de BAJO nivel, detalles de implementacion). Si mañana la validacion de login habla con
 * un backend real, este archivo no se entera ni se toca.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _uiState.value = AuthUiState.Loading
        // Sin Dispatchers.Default a proposito: InMemoryAuthRepository solo lee un mapa en
        // memoria (no hay IO real), y onSuccess() navega, lo cual debe correr en el hilo
        // principal. viewModelScope ya usa Dispatchers.Main.immediate por defecto.
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    userSessionRepository.login(result.profile)
                    _uiState.value = AuthUiState.Idle
                    onSuccess()
                }
                is AuthResult.Failure -> {
                    _uiState.value = AuthUiState.Error(result.reason)
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String, onSuccess: () -> Unit) {
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error(AuthFailureReason.INVALID_INPUT)
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.register(name, email, password)) {
                is AuthResult.Success -> {
                    userSessionRepository.login(result.profile)
                    _uiState.value = AuthUiState.Idle
                    onSuccess()
                }
                is AuthResult.Failure -> {
                    _uiState.value = AuthUiState.Error(result.reason)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}
