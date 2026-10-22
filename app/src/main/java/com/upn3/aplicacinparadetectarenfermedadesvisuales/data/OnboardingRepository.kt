package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [Principio D - DIP] Abstraccion de la que depende AppNavHost para decidir si mostrar el
 * onboarding antes de Login/Home. No sabe si ese estado vive en memoria o persistido en disco.
 */
interface OnboardingRepository {
    /** `true` cuando ya se termino de leer el estado persistido. Ver la misma idea en
     * [UserSessionRepository.isReady]: mientras sea `false`, [hasCompletedOnboarding] no es
     * confiable todavia. */
    val isReady: StateFlow<Boolean>
    val hasCompletedOnboarding: StateFlow<Boolean>
    fun markCompleted()
}

/**
 * [Principio S - SRP] Unica razon para cambiar: si el onboarding ya se mostro o no.
 * Implementacion de detalle (bajo nivel): en memoria, se vuelve a mostrar cada vez que arranca
 * el proceso. Reemplazada por `DataStoreOnboardingRepository` en AppContainer para que solo se
 * muestre una vez en la vida de la instalacion.
 */
class InMemoryOnboardingRepository : OnboardingRepository {
    override val isReady: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    override val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    override fun markCompleted() {
        _hasCompletedOnboarding.value = true
    }
}
