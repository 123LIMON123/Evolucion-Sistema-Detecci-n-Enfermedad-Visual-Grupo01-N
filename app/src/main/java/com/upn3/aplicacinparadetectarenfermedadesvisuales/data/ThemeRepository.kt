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

private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

/**
 * [Principio D - DIP] Abstraccion de la que depende `ProfileScreen` (el switch de modo oscuro) y
 * `MainActivity` (para elegir el ColorScheme). Ninguna de las dos sabe si la preferencia vive en
 * memoria o en disco.
 */
interface ThemeRepository {
    val isDarkMode: StateFlow<Boolean>
    fun toggle()
}

/**
 * [Principio S - SRP] Unica razon para cambiar: como se guarda/expone la preferencia de tema.
 *
 * Implementacion de detalle (bajo nivel): persiste en DataStore para que la preferencia
 * sobreviva a cerrar la app, igual que la sesion.
 */
class DataStoreThemeRepository(
    private val dataStore: DataStore<Preferences>,
    private val externalScope: CoroutineScope
) : ThemeRepository {

    override val isDarkMode: StateFlow<Boolean> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.e("DataStoreTheme", "No se pudo leer la preferencia de tema", error)
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { it[DARK_MODE_KEY] ?: false }
        .stateIn(externalScope, SharingStarted.Eagerly, false)

    override fun toggle() {
        externalScope.launch {
            try {
                dataStore.edit { it[DARK_MODE_KEY] = !(it[DARK_MODE_KEY] ?: false) }
            } catch (e: IOException) {
                Log.e("DataStoreTheme", "No se pudo guardar la preferencia de tema", e)
            }
        }
    }
}

/** Implementacion en memoria, util para pruebas o para un AppContainer sin DataStore. */
class InMemoryThemeRepository(initialDarkMode: Boolean = false) : ThemeRepository {
    private val _isDarkMode = MutableStateFlow(initialDarkMode)
    override val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    override fun toggle() {
        _isDarkMode.value = !_isDarkMode.value
    }
}
