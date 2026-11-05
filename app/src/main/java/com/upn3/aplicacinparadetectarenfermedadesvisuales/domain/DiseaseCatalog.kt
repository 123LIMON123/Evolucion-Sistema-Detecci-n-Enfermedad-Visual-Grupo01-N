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
        // Solo 3 de estas entradas corresponden a una salida real del modelo (mismo `code`
        // que usa TFLiteDiseaseClassifier: N, C, CONJ). La app le pide al usuario una foto del
        // ojo POR FUERA (camara normal del celular), no una foto de fondo de ojo (que requiere
        // una camara especializada apuntando a traves de la pupila dilatada). De las demas
        // condiciones de este catalogo, D/G/A/H/M son hallazgos de fondo de ojo (invisibles en
        // cualquier foto externa) y el astigmatismo es un error refractivo (invisible en
        // cualquier foto del ojo, ni externa ni de fondo): ninguna se puede detectar con este
        // modelo sin importar cuantos datos se sumen. Se dejan como contenido informativo; ver
        // ml/EXTERNAL_DATASETS.md para el detalle de esta decision.
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
                description = "Ojo externo sin signos visibles de catarata ni conjuntivitis.",
                symptoms = listOf("Vision clara", "Sin dolor", "Sin manchas"),
                recommendation = "Mantenga chequeos regulares anualmente."
            ),
            en = LocalizedDiseaseInfo(
                name = "Normal",
                description = "External eye with no visible signs of cataract or conjunctivitis.",
                symptoms = listOf("Clear vision", "No pain", "No spots"),
                recommendation = "Keep up with regular yearly checkups."
            )
        ),
        DiseaseInfo(
            code = "D",
            isDetectableByModel = false,
            es = LocalizedDiseaseInfo(
                name = "Diabetes",
                description = "La retinopatia diabetica es una complicacion de la diabetes que afecta los ojos. " +
                    "Sus signos (microaneurismas, hemorragias, exudados) estan dentro de la retina y solo se ven " +
                    "en una foto de fondo de ojo (camara especializada, pupila dilatada); esta app no puede " +
                    "detectarla a partir de una foto normal del ojo por fuera, se incluye aqui solo como " +
                    "informacion de referencia.",
                symptoms = listOf("Vision borrosa", "Manchas oscuras (flotadores)", "Dificultad para ver colores"),
                recommendation = "Controle sus niveles de azucar en sangre y visite a un oftalmologo."
            ),
            en = LocalizedDiseaseInfo(
                name = "Diabetes",
                description = "Diabetic retinopathy is a complication of diabetes that affects the eyes. Its signs " +
                    "(microaneurysms, hemorrhages, exudates) are inside the retina and only visible in a fundus " +
                    "photo (specialized camera, dilated pupil); this app cannot detect it from a normal external " +
                    "eye photo, it is included here for reference only.",
                symptoms = listOf("Blurred vision", "Dark spots (floaters)", "Difficulty seeing colors"),
                recommendation = "Control your blood sugar levels and visit an ophthalmologist."
            )
        ),
        DiseaseInfo(
            code = "G",
            isDetectableByModel = false,
            es = LocalizedDiseaseInfo(
                name = "Glaucoma",
                description = "Grupo de condiciones oculares que danan el nervio optico, a menudo por presion " +
                    "alta. Se diagnostica por el aspecto del nervio optico (fondo de ojo) y la presion " +
                    "intraocular, no por el aspecto externo del ojo; esta app no puede detectarlo a partir de " +
                    "una foto normal del ojo por fuera, se incluye aqui solo como informacion de referencia.",
                symptoms = listOf("Perdida de vision periferica", "Vision de tunel", "Dolor ocular severo (en casos agudos)"),
                recommendation = "El tratamiento temprano puede prevenir la perdida total de la vision."
            ),
            en = LocalizedDiseaseInfo(
                name = "Glaucoma",
                description = "A group of eye conditions that damage the optic nerve, often from high pressure. " +
                    "It is diagnosed from the appearance of the optic nerve (fundus) and eye pressure, not from " +
                    "the external appearance of the eye; this app cannot detect it from a normal external eye " +
                    "photo, it is included here for reference only.",
                symptoms = listOf("Loss of peripheral vision", "Tunnel vision", "Severe eye pain (in acute cases)"),
                recommendation = "Early treatment can prevent total vision loss."
            )
        ),
        DiseaseInfo(
            code = "C",
            es = LocalizedDiseaseInfo(
                name = "Catarata",
                description = "Opacidad del cristalino del ojo que normalmente es transparente. En estadio " +
                    "avanzado se ve como una opacidad blanquecina en la pupila en una foto normal del ojo por " +
                    "fuera, por eso es una de las condiciones que este modelo si puede evaluar.",
                symptoms = listOf("Vision nublada", "Sensibilidad a la luz", "Dificultad para ver de noche"),
                recommendation = "La cirugia de cataratas es un procedimiento comun y seguro."
            ),
            en = LocalizedDiseaseInfo(
                name = "Cataract",
                description = "Clouding of the eye's normally clear lens. In an advanced stage it shows as a " +
                    "whitish opacity in the pupil in a normal external eye photo, which is why this is one of " +
                    "the conditions this model can actually evaluate.",
                symptoms = listOf("Cloudy vision", "Sensitivity to light", "Difficulty seeing at night"),
                recommendation = "Cataract surgery is a common and safe procedure."
            )
        ),
        DiseaseInfo(
            code = "A",
            isDetectableByModel = false,
            es = LocalizedDiseaseInfo(
                name = "Degeneración Macular",
                description = "La degeneracion macular relacionada con la edad afecta la vision central " +
                    "detallada. Ocurre debajo de la retina y solo se ve en una foto de fondo de ojo; esta app no " +
                    "puede detectarla a partir de una foto normal del ojo por fuera, se incluye aqui solo como " +
                    "informacion de referencia.",
                symptoms = listOf("Distorsion de lineas rectas", "Mancha oscura en el centro del campo visual"),
                recommendation = "Consuma una dieta rica en antioxidantes y use proteccion UV."
            ),
            en = LocalizedDiseaseInfo(
                name = "Age-related Macular Degeneration",
                description = "Age-related macular degeneration affects detailed central vision. It happens " +
                    "beneath the retina and is only visible in a fundus photo; this app cannot detect it from a " +
                    "normal external eye photo, it is included here for reference only.",
                symptoms = listOf("Distortion of straight lines", "Dark spot in the center of the visual field"),
                recommendation = "Eat an antioxidant-rich diet and use UV protection."
            )
        ),
        DiseaseInfo(
            code = "H",
            isDetectableByModel = false,
            es = LocalizedDiseaseInfo(
                name = "Hipertensión",
                description = "La hipertension puede danar los vasos sanguineos de la retina. Ese dano solo es " +
                    "visible en una foto de fondo de ojo; esta app no puede detectarlo a partir de una foto " +
                    "normal del ojo por fuera, se incluye aqui solo como informacion de referencia.",
                symptoms = listOf("Generalmente asintomatica al inicio", "Dolores de cabeza", "Vision doble"),
                recommendation = "Controle su presion arterial regularmente."
            ),
            en = LocalizedDiseaseInfo(
                name = "Hypertension",
                description = "Hypertension can damage the blood vessels in the retina. That damage is only " +
                    "visible in a fundus photo; this app cannot detect it from a normal external eye photo, it " +
                    "is included here for reference only.",
                symptoms = listOf("Usually asymptomatic at first", "Headaches", "Double vision"),
                recommendation = "Monitor your blood pressure regularly."
            )
        ),
        DiseaseInfo(
            code = "M",
            isDetectableByModel = false,
            es = LocalizedDiseaseInfo(
                name = "Miopía",
                description = "La miopia patologica puede causar cambios degenerativos en el fondo de ojo, pero " +
                    "la miopia en si es un error refractivo (forma/longitud del ojo), sin cambios visibles en " +
                    "ninguna foto del ojo; esta app no puede detectarla a partir de una foto, se incluye aqui " +
                    "solo como informacion de referencia.",
                symptoms = listOf("Vision lejana borrosa", "Fatiga visual", "Entrecerrar los ojos"),
                recommendation = "Use correccion optica y realice examenes de fondo de ojo periodicos."
            ),
            en = LocalizedDiseaseInfo(
                name = "Myopia",
                description = "Pathological myopia can cause degenerative changes in the fundus, but myopia " +
                    "itself is a refractive error (eye shape/length) with no visible changes in any eye photo; " +
                    "this app cannot detect it from a photo, it is included here for reference only.",
                symptoms = listOf("Blurred distance vision", "Eye strain", "Squinting"),
                recommendation = "Use optical correction and get periodic fundus exams."
            )
        ),
        DiseaseInfo(
            code = "O",
            isDetectableByModel = false,
            es = LocalizedDiseaseInfo(
                name = "Otra",
                description = "Otras anomalias oculares no clasificadas en las categorias anteriores, " +
                    "originalmente observadas en fotos de fondo de ojo. Esta app no puede detectarla a partir " +
                    "de una foto normal del ojo por fuera, se incluye aqui solo como informacion de referencia.",
                symptoms = listOf("Variable segun la anomalia detectada"),
                recommendation = "Consulte a un oftalmologo para una evaluacion detallada."
            ),
            en = LocalizedDiseaseInfo(
                name = "Other",
                description = "Other eye abnormalities not classified in the categories above, originally " +
                    "observed in fundus photos. This app cannot detect it from a normal external eye photo, it " +
                    "is included here for reference only.",
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
                    "reaccion alergica. Se detecta en una foto del ojo por fuera, igual que Normal y Catarata.",
                symptoms = listOf("Enrojecimiento del ojo", "Picazon o ardor", "Secrecion o legañas", "Sensacion de cuerpo extraño"),
                recommendation = "Evite tocarse los ojos y compartir toallas; consulte a un medico si no mejora en pocos dias o si hay dolor o cambios en la vision."
            ),
            en = LocalizedDiseaseInfo(
                name = "Conjunctivitis",
                description = "Inflammation of the conjunctiva (the membrane covering the white of the eye and the " +
                    "inside of the eyelids), usually caused by a viral or bacterial infection or an allergic " +
                    "reaction. It is detected from a photo of the outside of the eye, same as Normal and Cataract.",
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
