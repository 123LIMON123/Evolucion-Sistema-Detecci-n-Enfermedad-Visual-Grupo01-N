package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

enum class AuthFailureReason {
    INVALID_INPUT,
    EMAIL_ALREADY_REGISTERED,
    INVALID_CREDENTIALS
}

sealed interface AuthResult {
    data class Success(val profile: UserProfile) : AuthResult
    data class Failure(val reason: AuthFailureReason) : AuthResult
}

/**
 * [Principio D - DIP] Abstraccion de la que dependen las capas de arriba (AuthViewModel). Un
 * modulo de alto nivel (el ViewModel, la pantalla) no depende de "como se valida un login" en
 * concreto, depende de este contrato. Mañana [InMemoryAuthRepository] se puede reemplazar por
 * una implementacion que hable con un backend real (Firebase Auth, una API propia, etc.) sin
 * tocar el ViewModel ni las pantallas de Login/Register.
 */
interface AuthRepository {
    fun register(name: String, email: String, password: String): AuthResult
    fun login(email: String, password: String): AuthResult
}

/**
 * [Principio S - SRP] Unica responsabilidad: validar credenciales y guardar cuentas en memoria.
 * No sabe de Compose, de navegacion ni de como se muestra un error en pantalla.
 *
 * Implementacion de detalle (bajo nivel): valida y guarda las cuentas en un mapa en memoria (no
 * hay backend en este proyecto). Se agrega una cuenta demo para poder probar el login sin tener
 * que registrarse primero.
 */
class InMemoryAuthRepository : AuthRepository {

    private data class Account(val name: String, val email: String, val password: String)

    private val accountsByEmail = mutableMapOf(
        DEMO_EMAIL to Account(name = "Usuario Demo", email = DEMO_EMAIL, password = DEMO_PASSWORD)
    )

    override fun register(name: String, email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (name.isBlank() || normalizedEmail.isBlank() || password.length < MIN_PASSWORD_LENGTH) {
            return AuthResult.Failure(AuthFailureReason.INVALID_INPUT)
        }
        if (accountsByEmail.containsKey(normalizedEmail)) {
            return AuthResult.Failure(AuthFailureReason.EMAIL_ALREADY_REGISTERED)
        }
        accountsByEmail[normalizedEmail] = Account(name.trim(), normalizedEmail, password)
        return AuthResult.Success(UserProfile(name = name.trim(), email = normalizedEmail))
    }

    override fun login(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val account = accountsByEmail[normalizedEmail]
        return if (account != null && account.password == password) {
            AuthResult.Success(UserProfile(name = account.name, email = account.email))
        } else {
            AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
        }
    }

    companion object {
        const val DEMO_EMAIL = "demo@ocucheck.ai"
        const val DEMO_PASSWORD = "demo1234"
        private const val MIN_PASSWORD_LENGTH = 4
    }
}
