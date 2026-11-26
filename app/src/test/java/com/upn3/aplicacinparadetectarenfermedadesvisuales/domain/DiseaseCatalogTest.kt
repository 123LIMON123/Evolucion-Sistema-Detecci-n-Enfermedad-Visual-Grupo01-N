package com.upn3.aplicacinparadetectarenfermedadesvisuales.domain

import com.upn3.aplicacinparadetectarenfermedadesvisuales.l10n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El catalogo es la fuente de verdad que usan tanto el interprete clinico como la pantalla de
 * informacion de enfermedades; si falta una traduccion o un codigo del modelo, el usuario termina
 * viendo texto vacio o el codigo crudo en pantalla, asi que vale la pena cubrirlo con pruebas.
 */
class DiseaseCatalogTest {

    // Debe coincidir exactamente con LABEL_COLS en ml/train.py: si el modelo se vuelve a
    // entrenar con otro orden/cantidad de clases sin actualizar esto, esta prueba lo detecta.
    private val modelLabelOrder = listOf("N", "C", "CONJ")

    // Codigos que siguen en el catalogo como informacion de referencia pero que el modelo de
    // foto externa (N, C, CONJ) no puede detectar: D/G/A/H/M son hallazgos de fondo de ojo,
    // AST es un error refractivo, y PTER/DRY son condiciones externas visibles pero para las
    // que el modelo todavia no fue entrenado (v1.1). Todos tienen algo accionable (autotest o
    // prevencion real); ver ml/EXTERNAL_DATASETS.md.
    private val informationOnlyCodes = listOf("D", "G", "A", "H", "M", "AST", "PTER", "DRY")

    // "O" (Otra) y "PTOS" (Ptosis) se sacaron del catalogo: no son detectables, no tienen
    // autotest, y su unica "recomendacion" era generica ("consulte a un oftalmologo"), sin nada
    // especifico y accionable que el usuario pueda hacer en casa.
    private val removedCodes = listOf("O", "PTOS")

    @Test
    fun `every label the model can output exists in the catalog`() {
        modelLabelOrder.forEach { code ->
            assertNotNull("Falta el codigo '$code' en DiseaseCatalog", DiseaseCatalog.byCode(code))
        }
    }

    @Test
    fun `an unknown code returns null instead of throwing`() {
        assertNull(DiseaseCatalog.byCode("XX"))
    }

    @Test
    fun `every entry has both languages fully filled in`() {
        DiseaseCatalog.all.forEach { disease ->
            listOf(disease.es, disease.en).forEach { info ->
                assertTrue("'${disease.code}' tiene nombre vacio", info.name.isNotBlank())
                assertTrue("'${disease.code}' tiene descripcion vacia", info.description.isNotBlank())
                assertTrue("'${disease.code}' no tiene sintomas", info.symptoms.isNotEmpty())
                assertTrue("'${disease.code}' tiene recomendacion vacia", info.recommendation.isNotBlank())
            }
        }
    }

    @Test
    fun `localized returns the matching language`() {
        val info = DiseaseCatalog.byCode("C")!!
        assertEquals("Catarata", info.localized(AppLanguage.ES).name)
        assertEquals("Cataract", info.localized(AppLanguage.EN).name)
    }

    @Test
    fun `there are no duplicate codes`() {
        val codes = DiseaseCatalog.all.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `every model-output code is flagged as detectable`() {
        modelLabelOrder.forEach { code ->
            assertTrue(
                "'$code' es una salida real del modelo, no deberia estar marcado isDetectableByModel=false",
                DiseaseCatalog.byCode(code)!!.isDetectableByModel
            )
        }
    }

    @Test
    fun `astigmatism is informational only, not a model output`() {
        val astigmatism = DiseaseCatalog.byCode("AST")
        assertNotNull("El catalogo deberia incluir astigmatismo como entrada informativa", astigmatism)
        assertTrue(
            "Astigmatismo no deberia marcarse como detectable: no es visible en ninguna foto del ojo",
            astigmatism!!.isDetectableByModel.not()
        )
        assertTrue("'AST' no deberia estar entre las salidas del modelo", "AST" !in modelLabelOrder)
    }

    @Test
    fun `fundus-only codes are informational, not a model output`() {
        informationOnlyCodes.forEach { code ->
            val disease = DiseaseCatalog.byCode(code)
            assertNotNull("El catalogo deberia incluir '$code' como entrada informativa", disease)
            assertTrue(
                "'$code' no deberia marcarse como detectable: no es visible en una foto de ojo externo",
                disease!!.isDetectableByModel.not()
            )
            assertTrue("'$code' no deberia estar entre las salidas del modelo", code !in modelLabelOrder)
        }
    }

    @Test
    fun `every informational code has something actionable (self-test or prevention)`() {
        informationOnlyCodes.forEach { code ->
            val disease = DiseaseCatalog.byCode(code)!!
            assertNotNull(
                "'$code' no es detectable y no tiene selfTest: no deberia estar en el catalogo si no hay nada que hacer con esa info",
                disease.selfTest
            )
        }
    }

    @Test
    fun `codes with no detection, no self-test and no specific prevention were removed from the catalog`() {
        removedCodes.forEach { code ->
            assertNull(
                "'$code' no deberia estar en el catalogo: no es detectable, no tiene autotest, y su recomendacion era generica",
                DiseaseCatalog.byCode(code)
            )
        }
    }
}
