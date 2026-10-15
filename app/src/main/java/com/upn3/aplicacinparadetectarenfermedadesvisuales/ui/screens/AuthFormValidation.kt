package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/**
 * [Principio S - SRP] Unica responsabilidad: decidir si un texto tiene forma de correo valido.
 * No es una validacion exhaustiva (RFC 5322 es mucho mas permisivo/complejo); alcanza para
 * detectar el error mas comun del usuario: olvidarse la arroba o el dominio.
 */
fun isValidEmailFormat(email: String): Boolean = EMAIL_REGEX.matches(email.trim())
