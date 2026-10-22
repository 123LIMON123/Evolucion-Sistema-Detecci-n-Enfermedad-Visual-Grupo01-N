package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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

private val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

/**
 * Implementacion de detalle de [OnboardingRepository] que persiste en disco, con el mismo patron
 * que [DataStoreUserSessionRepository] (recibe el [DataStore] ya construido para poder probarse
 * con un archivo temporal, y trata un fallo de lectura como "onboarding no visto" en vez de
 * romper la pantalla de arranque).
 */
class DataStoreOnboardingRepository(
    private val dataStore: DataStore<Preferences>,
    private val externalScope: CoroutineScope
) : OnboardingRepository {

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    override val hasCompletedOnboarding: StateFlow<Boolean> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.e("DataStoreOnboarding", "No se pudo leer el estado del onboarding", error)
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { it[HAS_COMPLETED_ONBOARDING] ?: false }
        .onEach { _isReady.value = true }
        .stateIn(externalScope, SharingStarted.Eagerly, false)

    override fun markCompleted() {
        externalScope.launch {
            try {
                dataStore.edit { it[HAS_COMPLETED_ONBOARDING] = true }
            } catch (e: IOException) {
                Log.e("DataStoreOnboarding", "No se pudo guardar que el onboarding se completo", e)
            }
        }
    }
}
