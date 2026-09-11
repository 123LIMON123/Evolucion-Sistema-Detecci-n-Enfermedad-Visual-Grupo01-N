package com.upn3.aplicacinparadetectarenfermedadesvisuales.domain

import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppLanguage

/**
 * Fuente unica de verdad para los metadatos clinicos/descriptivos de cada enfermedad (nombre,
 * descripcion, sintomas, recomendacion), en espanol e ingles. Antes esta informacion estaba
 * duplicada entre el mapeo de codigos del clasificador y la pantalla de informacion; ahora todos
 * leen de aqui, y el idioma se resuelve en el momento de mostrarla, no al calcularla.
 *
 * No conoce nada de TFLite, ByteBuffers ni umbrales de riesgo.
 */
object DiseaseCatalog {

    data class LocalizedDiseaseInfo(
        val name: String,
        val description: String,
        val symptoms: List<String>,
        val recommendation: String
    )

    data class DiseaseInfo(
        val code: String,
        val es: LocalizedDiseaseInfo,
        val en: LocalizedDiseaseInfo
    ) {
        fun localized(language: AppLanguage): LocalizedDiseaseInfo = when (language) {
            AppLanguage.ES -> es
            AppLanguage.EN -> en
        }
    }

    private val diseases = listOf(
        DiseaseInfo(
            code = "N",
            es = LocalizedDiseaseInfo(
                name = "Normal",
                description = "Ojo sin anomalias detectables en el fondo de ojo.",
                symptoms = listOf("Vision clara", "Sin dolor", "Sin manchas"),
                recommendation = "Mantenga chequeos regulares anualmente."
            ),
            en = LocalizedDiseaseInfo(
                name = "Normal",
                description = "Eye with no detectable abnormalities in the fundus.",
                symptoms = listOf("Clear vision", "No pain", "No spots"),
                recommendation = "Keep up with regular yearly checkups."
            )
        ),
        DiseaseInfo(
            code = "D",
            es = LocalizedDiseaseInfo(
                name = "Diabetes",
                description = "La retinopatia diabetica es una complicacion de la diabetes que afecta los ojos.",
                symptoms = listOf("Vision borrosa", "Manchas oscuras (flotadores)", "Dificultad para ver colores"),
                recommendation = "Controle sus niveles de azucar en sangre y visite a un oftalmologo."
            ),
            en = LocalizedDiseaseInfo(
                name = "Diabetes",
                description = "Diabetic retinopathy is a complication of diabetes that affects the eyes.",
                symptoms = listOf("Blurred vision", "Dark spots (floaters)", "Difficulty seeing colors"),
                recommendation = "Control your blood sugar levels and visit an ophthalmologist."
            )
        ),
        DiseaseInfo(
            code = "G",
            es = LocalizedDiseaseInfo(
                name = "Glaucoma",
                description = "Grupo de condiciones oculares que danan el nervio optico, a menudo por presion alta.",
                symptoms = listOf("Perdida de vision periferica", "Vision de tunel", "Dolor ocular severo (en casos agudos)"),
                recommendation = "El tratamiento temprano puede prevenir la perdida total de la vision."
            ),
            en = LocalizedDiseaseInfo(
                name = "Glaucoma",
                description = "A group of eye conditions that damage the optic nerve, often from high pressure.",
                symptoms = listOf("Loss of peripheral vision", "Tunnel vision", "Severe eye pain (in acute cases)"),
                recommendation = "Early treatment can prevent total vision loss."
            )
        ),
        DiseaseInfo(
            code = "C",
            es = LocalizedDiseaseInfo(
                name = "Catarata",
                description = "Opacidad del cristalino del ojo que normalmente es transparente.",
                symptoms = listOf("Vision nublada", "Sensibilidad a la luz", "Dificultad para ver de noche"),
                recommendation = "La cirugia de cataratas es un procedimiento comun y seguro."
            ),
            en = LocalizedDiseaseInfo(
                name = "Cataract",
                description = "Clouding of the eye's normally clear lens.",
                symptoms = listOf("Cloudy vision", "Sensitivity to light", "Difficulty seeing at night"),
                recommendation = "Cataract surgery is a common and safe procedure."
            )
        ),
        DiseaseInfo(
            code = "A",
            es = LocalizedDiseaseInfo(
                name = "Degeneración Macular",
                description = "La degeneracion macular relacionada con la edad afecta la vision central detallada.",
                symptoms = listOf("Distorsion de lineas rectas", "Mancha oscura en el centro del campo visual"),
                recommendation = "Consuma una dieta rica en antioxidantes y use proteccion UV."
            ),
            en = LocalizedDiseaseInfo(
                name = "Age-related Macular Degeneration",
                description = "Age-related macular degeneration affects detailed central vision.",
                symptoms = listOf("Distortion of straight lines", "Dark spot in the center of the visual field"),
                recommendation = "Eat an antioxidant-rich diet and use UV protection."
            )
        ),
        DiseaseInfo(
            code = "H",
            es = LocalizedDiseaseInfo(
                name = "Hipertensión",
                description = "La hipertension puede danar los vasos sanguineos de la retina.",
                symptoms = listOf("Generalmente asintomatica al inicio", "Dolores de cabeza", "Vision doble"),
                recommendation = "Controle su presion arterial regularmente."
            ),
            en = LocalizedDiseaseInfo(
                name = "Hypertension",
                description = "Hypertension can damage the blood vessels in the retina.",
                symptoms = listOf("Usually asymptomatic at first", "Headaches", "Double vision"),
                recommendation = "Monitor your blood pressure regularly."
            )
        ),
        DiseaseInfo(
            code = "M",
            es = LocalizedDiseaseInfo(
                name = "Miopía",
                description = "La miopia patologica puede causar cambios degenerativos en el fondo de ojo.",
                symptoms = listOf("Vision lejana borrosa", "Fatiga visual", "Entrecerrar los ojos"),
                recommendation = "Use correccion optica y realice examenes de fondo de ojo periodicos."
            ),
            en = LocalizedDiseaseInfo(
                name = "Myopia",
                description = "Pathological myopia can cause degenerative changes in the fundus.",
                symptoms = listOf("Blurred distance vision", "Eye strain", "Squinting"),
                recommendation = "Use optical correction and get periodic fundus exams."
            )
        ),
        DiseaseInfo(
            code = "O",
            es = LocalizedDiseaseInfo(
                name = "Otra",
                description = "Otras anomalias oculares no clasificadas en las categorias anteriores.",
                symptoms = listOf("Variable segun la anomalia detectada"),
                recommendation = "Consulte a un oftalmologo para una evaluacion detallada."
            ),
            en = LocalizedDiseaseInfo(
                name = "Other",
                description = "Other eye abnormalities not classified in the categories above.",
                symptoms = listOf("Variable depending on the abnormality detected"),
                recommendation = "Consult an ophthalmologist for a detailed evaluation."
            )
        )
    )

    private val byCodeIndex: Map<String, DiseaseInfo> = diseases.associateBy { it.code }

    val all: List<DiseaseInfo> = diseases

    fun byCode(code: String): DiseaseInfo? = byCodeIndex[code]
}
