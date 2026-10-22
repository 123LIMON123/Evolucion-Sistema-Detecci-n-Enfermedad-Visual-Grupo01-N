package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Mismo enfoque que DataStoreUserSessionRepositoryTest: DataStore real sobre un archivo
 * temporal, colectado en backgroundScope, y se espera la condicion real con `first { }` en vez
 * de avanzar un scheduler virtual (ver esa clase para el detalle de por que). */
@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreOnboardingRepositoryTest {

    private fun TestScope.newRepository(file: File): DataStoreOnboardingRepository {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
        return DataStoreOnboardingRepository(dataStore, backgroundScope)
    }

    private fun newTempFile(): File {
        val file = File.createTempFile("onboarding_test", ".preferences_pb")
        file.deleteOnExit()
        return file
    }

    @Test
    fun `a new install has not completed onboarding`() = runTest {
        val repository = newRepository(newTempFile())
        repository.isReady.first { it }

        assertFalse(repository.hasCompletedOnboarding.value)
    }

    @Test
    fun `markCompleted persists true`() = runTest {
        val repository = newRepository(newTempFile())
        repository.isReady.first { it }

        repository.markCompleted()

        assertTrue(repository.hasCompletedOnboarding.first { it })
    }
}
