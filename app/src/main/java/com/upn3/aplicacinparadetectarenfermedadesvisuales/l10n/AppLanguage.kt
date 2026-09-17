package com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n

/**
 * [Principio S - SRP] Unica responsabilidad: enumerar los idiomas soportados por la UI. No sabe
 * nada de textos ni de como se guarda la preferencia (eso vive en `LocalizationRepository`).
 */
enum class AppLanguage { ES, EN }
