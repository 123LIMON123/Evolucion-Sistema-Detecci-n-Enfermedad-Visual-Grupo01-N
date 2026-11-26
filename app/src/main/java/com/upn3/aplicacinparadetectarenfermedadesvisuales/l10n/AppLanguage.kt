package com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n

/**
 * [Principio S - SRP] Unica responsabilidad: enumerar los idiomas soportados por la UI. No sabe
 * nada de textos ni de como se guarda la preferencia (eso vive en `LocalizationRepository`).
 *
 * [Principio O - OCP] Agregar un idioma nuevo es agregar un valor aqui (y su `AppStrings` en
 * l10n/AppStrings.kt): [next] y [shortCode] ya ciclan sobre [entries] sin necesidad de tocarlos.
 */
enum class AppLanguage {
    ES, EN,

    /** Quechua (variante sureña / chanka-qusqu-qullaw). Los terminos medicos modernos sin
     * equivalente tradicional (catarata, conjuntivitis, oftalmologo, etc.) se dejan en castellano,
     * igual que en el uso real de hablantes de quechua en contextos de salud en el Peru. */
    QU
}

/** El siguiente idioma en el ciclo ES -> EN -> QU -> ES, usado por el boton de idioma de [AppTopBar]. */
fun AppLanguage.next(): AppLanguage {
    val values = AppLanguage.entries
    return values[(ordinal + 1) % values.size]
}

/** Codigo corto para mostrar en el boton de idioma (no depende de ningun [AppStrings]). */
fun AppLanguage.shortCode(): String = when (this) {
    AppLanguage.ES -> "ES"
    AppLanguage.EN -> "EN"
    AppLanguage.QU -> "QU"
}

/**
 * Nombre del idioma en si mismo (no depende de [AppStrings]/del idioma activo): se usa en el
 * selector de Ajustes para que las 3 opciones queden siempre visibles, nunca escondidas detras
 * de un ciclo de "siguiente idioma" que habia que tocar varias veces para notar que existian.
 */
fun AppLanguage.displayName(): String = when (this) {
    AppLanguage.ES -> "Español"
    AppLanguage.EN -> "English"
    AppLanguage.QU -> "Runasimi (quechua)"
}
