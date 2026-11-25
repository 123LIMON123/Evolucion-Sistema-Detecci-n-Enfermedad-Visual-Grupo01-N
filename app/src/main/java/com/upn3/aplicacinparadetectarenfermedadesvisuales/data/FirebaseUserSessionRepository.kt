package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private fun FirebaseUser?.toUserProfile(): UserProfile =
    UserProfile(name = this?.displayName ?: "", email = this?.email ?: "")

/**
 * [Principio D - DIP] Implementacion de detalle de [UserSessionRepository] que usa
 * [FirebaseAuth] como fuente de verdad de la sesion (en vez de un mapa en memoria o DataStore
 * propios): la sesion sobrevive a cerrar la app porque FirebaseAuth ya persiste su propio token,
 * y se comparte entre dispositivos si el usuario inicia sesion en otro con la misma cuenta.
 *
 * [login] no hace nada por si solo: lo deja vacio a proposito porque [FirebaseAuthRepository] ya
 * dejo a FirebaseAuth con la sesion abierta antes de llegar aca; el [AuthStateListener] de abajo
 * es el que entera a esta clase (y a quien la observe) del cambio real.
 */
class FirebaseUserSessionRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val externalScope: CoroutineScope
) : UserSessionRepository {

    override val isReady: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    private val _isLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser.toUserProfile())
    override val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _isLoggedIn.value = auth.currentUser != null
            _currentUser.value = auth.currentUser.toUserProfile()
        }
    }

    override fun login(profile: UserProfile) {
        // Intencionalmente vacio: ver KDoc de la clase.
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun updateProfile(name: String, email: String) {
        val user = firebaseAuth.currentUser ?: return
        externalScope.launch {
            try {
                user.updateProfile(userProfileChangeRequest { displayName = name }).await()
                // updateProfile no dispara el AuthStateListener (el uid/token no cambia, solo
                // metadata): se recarga el usuario y se empuja el estado nuevo a mano para que la
                // UI lo vea de inmediato, en vez de esperar al proximo evento real de auth.
                user.reload().await()
                _currentUser.value = firebaseAuth.currentUser.toUserProfile()
                firestore.collection("users").document(user.uid)
                    .set(mapOf("name" to name), SetOptions.merge()).await()
                // Nota: el correo de la cuenta NO se cambia aqui. Firebase exige reverificar por
                // email (verifyBeforeUpdateEmail) antes de aplicar un cambio real de correo; no
                // se puede hacer "al toque" como la version en memoria/DataStore. Si el usuario
                // edita el campo de correo en Perfil, ese cambio no se aplica a la cuenta real.
            } catch (e: Exception) {
                Log.e("FirebaseUserSession", "No se pudo actualizar el perfil", e)
            }
        }
    }
}
