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
    private val modelLabelOrder = listOf("N", "D", "G", "C", "A", "H", "M", "O")

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
}
