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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

private val REMINDERS_ENABLED_KEY = booleanPreferencesKey("reminders_enabled")

/**
 * [Principio D - DIP] Abstraccion de la que depende `SettingsScreen` (el switch de recordatorios).
 * Solo guarda/expone la preferencia; no sabe nada de notificaciones ni de WorkManager, eso es
 * responsabilidad de `ReminderScheduler`.
 */
interface ReminderRepository {
    val isEnabled: StateFlow<Boolean>
    fun setEnabled(enabled: Boolean)
}

/**
 * [Principio S - SRP] Unica razon para cambiar: como se guarda/expone la preferencia de
 * recordatorios. Persistida en DataStore para sobrevivir a cerrar la app, igual que el tema.
 */
class DataStoreReminderRepository(
    private val dataStore: DataStore<Preferences>,
    private val externalScope: CoroutineScope
) : ReminderRepository {

    override val isEnabled: StateFlow<Boolean> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.e("DataStoreReminder", "No se pudo leer la preferencia de recordatorios", error)
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { it[REMINDERS_ENABLED_KEY] ?: false }
        .stateIn(externalScope, SharingStarted.Eagerly, false)

    override fun setEnabled(enabled: Boolean) {
        externalScope.launch {
            try {
                dataStore.edit { it[REMINDERS_ENABLED_KEY] = enabled }
            } catch (e: IOException) {
                Log.e("DataStoreReminder", "No se pudo guardar la preferencia de recordatorios", e)
            }
        }
    }
}

/** Implementacion en memoria, util para pruebas. */
class InMemoryReminderRepository(initialEnabled: Boolean = false) : ReminderRepository {
    private val _isEnabled = MutableStateFlow(initialEnabled)
    override val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    override fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }
}
