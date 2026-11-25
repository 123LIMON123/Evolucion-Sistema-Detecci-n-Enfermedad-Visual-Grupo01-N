package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * [Principio D - DIP] Implementacion de detalle de [AuthRepository] que habla con Firebase
 * Authentication de verdad (correo/contraseña + Google Sign-In), en vez de validar contra un mapa
 * en memoria. Ninguna pantalla ni [com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel.AuthViewModel]
 * se entera del cambio: siguen dependiendo solo de la interfaz.
 *
 * Tambien escribe un documento `users/{uid}` en Firestore (nombre, correo) en cada registro/login
 * nuevo: es el punto donde la autenticacion "se conecta" con la base de datos, para que haya un
 * registro del usuario mas alla de lo que guarda FirebaseAuth internamente.
 */
class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String): AuthResult {
        val trimmedName = name.trim()
        val normalizedEmail = email.trim().lowercase()
        if (trimmedName.isBlank() || normalizedEmail.isBlank() || password.length < MIN_PASSWORD_LENGTH) {
            return AuthResult.Failure(AuthFailureReason.INVALID_INPUT)
        }
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(normalizedEmail, password).await()
            val user = result.user ?: return AuthResult.Failure(AuthFailureReason.INVALID_INPUT)
            user.updateProfile(userProfileChangeRequest { displayName = trimmedName }).await()
            saveUserDocument(user.uid, trimmedName, normalizedEmail)
            AuthResult.Success(UserProfile(name = trimmedName, email = normalizedEmail))
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Failure(AuthFailureReason.EMAIL_ALREADY_REGISTERED)
        } catch (e: Exception) {
            AuthResult.Failure(AuthFailureReason.INVALID_INPUT)
        }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(normalizedEmail, password).await()
            val user = result.user ?: return AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
            AuthResult.Success(UserProfile(name = user.displayName ?: "", email = user.email ?: normalizedEmail))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
        } catch (e: Exception) {
            AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
            val name = user.displayName ?: ""
            val userEmail = user.email ?: ""
            saveUserDocument(user.uid, name, userEmail)
            AuthResult.Success(UserProfile(name = name, email = userEmail))
        } catch (e: Exception) {
            AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
        }
    }

    override suspend fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String
    ): PasswordChangeResult {
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return PasswordChangeResult.Failure(PasswordChangeFailureReason.NEW_PASSWORD_TOO_SHORT)
        }
        val user = firebaseAuth.currentUser
            ?: return PasswordChangeResult.Failure(PasswordChangeFailureReason.WRONG_CURRENT_PASSWORD)
        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email.trim().lowercase(), currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            PasswordChangeResult.Success
        } catch (e: Exception) {
            PasswordChangeResult.Failure(PasswordChangeFailureReason.WRONG_CURRENT_PASSWORD)
        }
    }

    private suspend fun saveUserDocument(uid: String, name: String, email: String) {
        val data = mapOf(
            "name" to name,
            "email" to email,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection(USERS_COLLECTION).document(uid).set(data, SetOptions.merge()).await()
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        // Firebase Auth rechaza contraseñas de menos de 6 caracteres: se valida igual aca para
        // dar el error en espanol/ingles en vez del mensaje generico de Firebase.
        const val MIN_PASSWORD_LENGTH = 6
    }
}
