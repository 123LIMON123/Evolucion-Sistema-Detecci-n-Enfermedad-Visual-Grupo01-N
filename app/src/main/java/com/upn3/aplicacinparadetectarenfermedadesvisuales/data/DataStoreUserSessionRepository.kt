package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

private object SessionKeys {
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val NAME = stringPreferencesKey("name")
    val EMAIL = stringPreferencesKey("email")
}

private data class PersistedSession(val isLoggedIn: Boolean, val profile: UserProfile)

private fun Preferences.toPersistedSession() = PersistedSession(
    isLoggedIn = this[SessionKeys.IS_LOGGED_IN] ?: false,
    profile = UserProfile(name = this[SessionKeys.NAME] ?: "", email = this[SessionKeys.EMAIL] ?: "")
)

/**
 * [Principio S - SRP] Unica razon para cambiar: como se persiste la sesion en disco. No sabe
 * nada de Compose, de navegacion ni de como se valida un login; solo lee/escribe un
 * [DataStore] de preferencias ya construido.
 *
 * Implementacion de detalle (bajo nivel) de [UserSessionRepository]: a diferencia de
 * [InMemoryUserSessionRepository], la sesion sobrevive a cerrar la app. Recibe el [DataStore]
 * ya armado (en vez de un `Context`) para no depender de Android: en una prueba se le puede
 * pasar un `PreferenceDataStoreFactory.create(...)` apuntando a un archivo temporal, sin
 * necesitar Robolectric ni un dispositivo.
 */
class DataStoreUserSessionRepository(
    private val dataStore: DataStore<Preferences>,
    private val externalScope: CoroutineScope
) : UserSessionRepository {

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    // Una sola coleccion del archivo de preferencias, compartida por isLoggedIn y currentUser
    // (antes cada uno abria su propia coleccion del mismo flujo). Si el archivo de preferencias
    // no se puede leer (corrupto, error de IO), se trata como "sesion vacia" en vez de romper
    // la pantalla de arranque.
    private val session: StateFlow<PersistedSession> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.e("DataStoreUserSession", "No se pudo leer la sesion persistida", error)
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { it.toPersistedSession() }
        .onEach { _isReady.value = true }
        .stateIn(externalScope, SharingStarted.Eagerly, PersistedSession(false, UserProfile("", "")))

    override val isLoggedIn: StateFlow<Boolean> =
        session.mapState(externalScope) { it.isLoggedIn }

    override val currentUser: StateFlow<UserProfile> =
        session.mapState(externalScope) { it.profile }

    override fun login(profile: UserProfile) {
        writeSession {
            it[SessionKeys.IS_LOGGED_IN] = true
            it[SessionKeys.NAME] = profile.name
            it[SessionKeys.EMAIL] = profile.email
        }
    }

    override fun logout() {
        // Se borran tambien nombre y correo, no solo la bandera: de lo contrario quedarian
        // legibles en el archivo de preferencias despues de un logout explicito (ej. en un
        // dispositivo compartido entre varios usuarios).
        writeSession {
            it.remove(SessionKeys.IS_LOGGED_IN)
            it.remove(SessionKeys.NAME)
            it.remove(SessionKeys.EMAIL)
        }
    }

    private fun writeSession(edit: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        externalScope.launch {
            try {
                dataStore.edit(edit)
            } catch (e: IOException) {
                // Una escritura de sesion que falla (disco lleno, permiso denegado) no deberia
                // tumbar la app: el estado en memoria sigue siendo el que el usuario ve hasta
                // que cierre la app, aunque no haya quedado guardado para la proxima vez.
                Log.e("DataStoreUserSession", "No se pudo guardar la sesion", e)
            }
        }
    }
}

/** Deriva un StateFlow nuevo de otro sin abrir una coleccion nueva del origen (DataStore). */
private fun <T, R> StateFlow<T>.mapState(scope: CoroutineScope, transform: (T) -> R): StateFlow<R> =
    map(transform).stateIn(scope, SharingStarted.Eagerly, transform(value))
