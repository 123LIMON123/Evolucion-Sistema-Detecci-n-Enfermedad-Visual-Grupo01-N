package com.upn3.aplicacinparadetectarenfermedadesvisuales.domain

/**
 * [Principio S - SRP] Unica responsabilidad: nombrar que autotest casero (si alguno) corresponde
 * a cada [DiseaseCatalog.DiseaseInfo]. Son pruebas de cribado conocidas en oftalmologia, no
 * inventadas para esta app, y ninguna reemplaza un examen profesional.
 */
enum class SelfTestType {
    /** Cartilla de agudeza visual (tipo Snellen) calibrada a distancia real y tamano fisico de pantalla. */
    VISUAL_ACUITY,

    /** Rejilla de Amsler: detecta distorsion/perdida de vision central (signo de DMAE). */
    AMSLER_GRID,

    /** Disco/abanico de astigmatismo: lineas radiales para notar si el ojo enfoca distinto segun el eje. */
    ASTIGMATISM_FAN,

    /** Cuestionario de sintomas de ojo seco (frecuencia de molestias). */
    DRY_EYE_QUESTIONNAIRE,

    /**
     * No existe un autotest casero confiable para esta condicion (requiere equipo clinico:
     * tonometro, fondo de ojo dilatado, control de presion arterial o glucosa). Se muestra una
     * recomendacion honesta en vez de inventar una prueba.
     */
    NO_RELIABLE_SELF_TEST
}
