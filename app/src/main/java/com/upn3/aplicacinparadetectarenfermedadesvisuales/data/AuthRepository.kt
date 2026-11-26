package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

enum class AuthFailureReason {
    INVALID_INPUT,
    EMAIL_ALREADY_REGISTERED,
    INVALID_CREDENTIALS
}

sealed interface AuthResult {
    /**
     * @param isNewUser `true` solo cuando la cuenta se acaba de crear en este mismo llamado (ej.
     * primer login con Google). La pantalla lo usa para pedir nombre/apellidos una unica vez, en
     * vez de interrumpir a alguien que ya tenia cuenta con un formulario de mas.
     */
    data class Success(val profile: UserProfile, val isNewUser: Boolean = false) : AuthResult
    data class Failure(val reason: AuthFailureReason) : AuthResult
}

enum class PasswordChangeFailureReason {
    WRONG_CURRENT_PASSWORD,
    NEW_PASSWORD_TOO_SHORT
}

sealed interface PasswordChangeResult {
    data object Success : PasswordChangeResult
    data class Failure(val reason: PasswordChangeFailureReason) : PasswordChangeResult
}

/**
 * [Principio D - DIP] Abstraccion de la que dependen las capas de arriba (AuthViewModel). Un
 * modulo de alto nivel (el ViewModel, la pantalla) no depende de "como se valida un login" en
 * concreto, depende de este contrato. Mañana [InMemoryAuthRepository] se puede reemplazar por
 * una implementacion que hable con un backend real (Firebase Auth, una API propia, etc.) sin
 * tocar el ViewModel ni las pantallas de Login/Register.
 */
interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    /** Inicia sesion con la credencial de Google (el ID token que entrega el flujo de Google Sign-In). */
    suspend fun signInWithGoogleIdToken(idToken: String): AuthResult
    suspend fun changePassword(email: String, currentPassword: String, newPassword: String): PasswordChangeResult
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

    override suspend fun register(name: String, email: String, password: String): AuthResult {
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

    override suspend fun login(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val account = accountsByEmail[normalizedEmail]
        return if (account != null && account.password == password) {
            AuthResult.Success(UserProfile(name = account.name, email = account.email))
        } else {
            AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
        }
    }

    // No hay flujo de Google Sign-In sin un backend real detras: esta implementacion en memoria
    // es solo para pruebas/demo, asi que siempre rechaza.
    override suspend fun signInWithGoogleIdToken(idToken: String): AuthResult =
        AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)

    override suspend fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String
    ): PasswordChangeResult {
        val normalizedEmail = email.trim().lowercase()
        val account = accountsByEmail[normalizedEmail]
        if (account == null || account.password != currentPassword) {
            return PasswordChangeResult.Failure(PasswordChangeFailureReason.WRONG_CURRENT_PASSWORD)
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return PasswordChangeResult.Failure(PasswordChangeFailureReason.NEW_PASSWORD_TOO_SHORT)
        }
        accountsByEmail[normalizedEmail] = account.copy(password = newPassword)
        return PasswordChangeResult.Success
    }

    companion object {
        const val DEMO_EMAIL = "demo@ocucheck.ai"
        const val DEMO_PASSWORD = "demo1234"
        // Alineado al minimo real que exige Firebase Auth (rechaza contraseñas de menos de 6
        // caracteres), para que la validacion del formulario y la del backend coincidan.
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
