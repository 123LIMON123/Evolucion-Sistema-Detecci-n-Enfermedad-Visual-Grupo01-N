package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Usa un [PreferenceDataStoreFactory] real apuntando a un archivo temporal, no un Context de
 * Android: por eso [DataStoreUserSessionRepository] recibe el [androidx.datastore.core.DataStore]
 * ya construido en vez de un Context. Esto deja probar la persistencia de verdad (escribe y lee
 * de un archivo) sin Robolectric ni un dispositivo/emulador.
 *
 * Dos detalles de por que las pruebas estan escritas con `first { condicion }` en vez de
 * `advanceUntilIdle()` + leer `.value`:
 * 1. El repositorio colecciona el DataStore de forma "eager" (vive mientras viva la app, nunca
 *    termina solo), asi que cada prueba lo arma en [TestScope.backgroundScope]: `runTest`
 *    cancela ese scope automaticamente al terminar en vez de exigir que la coleccion termine.
 * 2. DataStore hace la lectura/escritura real del archivo en su propio dispatcher interno, no
 *    en el scheduler virtual del test, asi que avanzar el scheduler no garantiza que ya haya
 *    terminado. Esperar la condicion real con `first { }` es mas robusto que adivinar.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreUserSessionRepositoryTest {

    private fun TestScope.newRepository(file: File): DataStoreUserSessionRepository {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
        return DataStoreUserSessionRepository(dataStore, backgroundScope)
    }

    private fun newTempFile(): File {
        val file = File.createTempFile("user_session_test", ".preferences_pb")
        file.deleteOnExit()
        return file
    }

    @Test
    fun `becomes ready after the first read of the preferences file`() = runTest {
        val repository = newRepository(newTempFile())
        assertTrue(repository.isReady.first { it })
    }

    @Test
    fun `a brand-new session starts logged out with an empty profile`() = runTest {
        val repository = newRepository(newTempFile())
        repository.isReady.first { it }

        assertEquals(false, repository.isLoggedIn.value)
        assertEquals(UserProfile(name = "", email = ""), repository.currentUser.value)
    }

    @Test
    fun `login persists isLoggedIn and the profile`() = runTest {
        val repository = newRepository(newTempFile())
        repository.isReady.first { it }

        repository.login(UserProfile(name = "Ada", email = "ada@example.com"))

        assertTrue(repository.isLoggedIn.first { it })
        assertEquals(
            UserProfile(name = "Ada", email = "ada@example.com"),
            repository.currentUser.first { it.name == "Ada" }
        )
    }

    @Test
    fun `logout clears the profile too, not just the logged-in flag`() = runTest {
        val repository = newRepository(newTempFile())
        repository.isReady.first { it }
        repository.login(UserProfile(name = "Ada", email = "ada@example.com"))
        repository.isLoggedIn.first { it }

        repository.logout()

        assertEquals(false, repository.isLoggedIn.first { !it })
        assertEquals(
            "El nombre y el correo deben borrarse al cerrar sesion, no solo la bandera",
            UserProfile(name = "", email = ""),
            repository.currentUser.first { it.name.isEmpty() }
        )
    }
}
