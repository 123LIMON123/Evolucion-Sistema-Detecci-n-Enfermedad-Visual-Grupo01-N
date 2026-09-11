package com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.LocalizationRepository

val LocalAppLanguage = compositionLocalOf { AppLanguage.ES }
val LocalAppStrings = compositionLocalOf { EsStrings }

/**
 * Expone el idioma activo y sus strings a todo el arbol de composicion, para no tener que pasar
 * [LocalizationRepository] como parametro a cada pantalla. Un solo punto de wiring (MainActivity).
 */
@Composable
fun ProvideLocalization(
    localizationRepository: LocalizationRepository,
    content: @Composable () -> Unit
) {
    val language by localizationRepository.language.collectAsState()
    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalAppStrings provides language.strings(),
        content = content
    )
}
