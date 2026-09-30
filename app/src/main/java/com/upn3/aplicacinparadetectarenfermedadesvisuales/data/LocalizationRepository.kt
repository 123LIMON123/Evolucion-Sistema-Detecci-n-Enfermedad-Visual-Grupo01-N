package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [Principio D - DIP] Abstraccion de la que dependen todas las pantallas (via `AppTopBar`).
 * Ninguna conoce [InMemoryLocalizationRepository]; si mañana el idioma se guarda en
 * SharedPreferences para recordarlo entre sesiones, se escribe una implementacion nueva y solo
 * se cambia una linea en `AppContainer`.
 */
interface LocalizationRepository {
    val language: StateFlow<AppLanguage>
    fun setLanguage(language: AppLanguage)
    fun toggle()
}

/**
 * [Principio S - SRP] Unica razón para cambiar: como se guarda/expone el idioma preferido de la
 * UI. No sabe nada de los textos en si (eso vive en el catálogo de strings), solo de cual está
 * activo.
 *
 * Implementacion de detalle (bajo nivel): guarda el idioma en memoria mientras el proceso vive.
 */
class InMemoryLocalizationRepository(initialLanguage: AppLanguage = AppLanguage.ES) : LocalizationRepository {

    private val _language = MutableStateFlow(initialLanguage)
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        _language.value = language
    }

    override fun toggle() {
        _language.value = if (_language.value == AppLanguage.ES) AppLanguage.EN else AppLanguage.ES
    }
}
