package com.upn3.aplicacinparadetectarenfermedadesvisuales.domain

import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppLanguage

/**
 * [Principio S - SRP] Unica responsabilidad: ser la fuente unica de verdad para los metadatos
 * clinicos/descriptivos de cada enfermedad (nombre,
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
        val en: LocalizedDiseaseInfo,
        // La mayoria de estas entradas corresponden a una de las 9 salidas del modelo (mismo
        // `code` que usa TFLiteDiseaseClassifier: 8 de fondo de ojo + CONJ de foto de ojo
        // externo). El astigmatismo es la excepcion: es un error refractivo (forma de la
        // cornea), no una condicion visible en ninguna foto del ojo (ni de fondo ni externa),
        // asi que no tiene sentido clinico que un modelo entrenado con fotos intente
        // "detectarlo". Se deja como contenido puramente informativo.
        val isDetectableByModel: Boolean = true
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
        ),
        DiseaseInfo(
            code = "CONJ",
            es = LocalizedDiseaseInfo(
                name = "Conjuntivitis",
                description = "Inflamacion de la conjuntiva (la membrana que cubre la parte blanca del ojo y el " +
                    "interior de los parpados), generalmente causada por una infeccion viral, bacteriana o una " +
                    "reaccion alergica. A diferencia de las otras 8 clases, se detecta en una foto del ojo por " +
                    "fuera, no de fondo de ojo.",
                symptoms = listOf("Enrojecimiento del ojo", "Picazon o ardor", "Secrecion o legañas", "Sensacion de cuerpo extraño"),
                recommendation = "Evite tocarse los ojos y compartir toallas; consulte a un medico si no mejora en pocos dias o si hay dolor o cambios en la vision."
            ),
            en = LocalizedDiseaseInfo(
                name = "Conjunctivitis",
                description = "Inflammation of the conjunctiva (the membrane covering the white of the eye and the " +
                    "inside of the eyelids), usually caused by a viral or bacterial infection or an allergic " +
                    "reaction. Unlike the other 8 classes, it is detected from a photo of the outside of the eye, " +
                    "not a fundus photo.",
                symptoms = listOf("Redness of the eye", "Itching or burning", "Discharge or crusting", "Feeling of a foreign body"),
                recommendation = "Avoid touching your eyes and sharing towels; see a doctor if it doesn't improve in a few days or if there is pain or vision changes."
            )
        ),
        DiseaseInfo(
            code = "AST",
            isDetectableByModel = false,
            es = LocalizedDiseaseInfo(
                name = "Astigmatismo",
                description = "Error refractivo causado por una curvatura irregular de la cornea o el cristalino. " +
                    "A diferencia de las demas condiciones de este catalogo, no produce cambios visibles en el " +
                    "fondo de ojo, por lo que esta app no puede detectarlo a partir de una foto: se incluye aqui " +
                    "solo como informacion de referencia.",
                symptoms = listOf("Vision borrosa o distorsionada a cualquier distancia", "Fatiga visual", "Dolores de cabeza", "Entrecerrar los ojos con frecuencia"),
                recommendation = "Se diagnostica con un examen de refraccion (no con una foto); consulte a un optometrista u oftalmologo."
            ),
            en = LocalizedDiseaseInfo(
                name = "Astigmatism",
                description = "A refractive error caused by an irregularly shaped cornea or lens. Unlike the other " +
                    "conditions in this catalog, it causes no visible changes in the fundus, so this app cannot " +
                    "detect it from a photo: it is included here for reference only.",
                symptoms = listOf("Blurred or distorted vision at any distance", "Eye strain", "Headaches", "Frequent squinting"),
                recommendation = "Diagnosed with a refraction exam, not a photo; consult an optometrist or ophthalmologist."
            )
        )
    )

    private val byCodeIndex: Map<String, DiseaseInfo> = diseases.associateBy { it.code }

    val all: List<DiseaseInfo> = diseases

    fun byCode(code: String): DiseaseInfo? = byCodeIndex[code]
}
