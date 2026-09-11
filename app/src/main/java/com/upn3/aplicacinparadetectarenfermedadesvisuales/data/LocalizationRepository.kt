package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unica razon para cambiar: como se guarda/expone el idioma preferido de la UI. No sabe nada de
 * los textos en si (eso vive en el catalogo de strings), solo de cual esta activo.
 */
class LocalizationRepository(initialLanguage: AppLanguage = AppLanguage.ES) {

    private val _language = MutableStateFlow(initialLanguage)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _language.value = language
    }

    fun toggle() {
        _language.value = if (_language.value == AppLanguage.ES) AppLanguage.EN else AppLanguage.ES
    }
}
