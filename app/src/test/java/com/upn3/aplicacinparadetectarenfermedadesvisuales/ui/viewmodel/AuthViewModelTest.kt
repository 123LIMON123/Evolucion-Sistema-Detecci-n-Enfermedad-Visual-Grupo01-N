package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.viewmodel

import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthFailureReason
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthRepository
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.AuthResult
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.UserProfile
import com.upn3.aplicacinparadetectarenfermedadesvisuales.data.UserSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Doble de prueba de [AuthRepository]: siempre devuelve lo que se le configuro, sin tocar un
 * mapa en memoria real ni ninguna validacion. Esto es exactamente lo que permite el principio D
 * (Inversion de Dependencia): [AuthViewModel] depende de la interfaz, asi que en el test se le
 * puede inyectar cualquier implementacion, incluida una falsa y controlada.
 */
private class FakeAuthRepository : AuthRepository {
    var loginResult: AuthResult = AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
    var registerResult: AuthResult = AuthResult.Failure(AuthFailureReason.INVALID_INPUT)
    var lastLoginEmail: String? = null

    override fun register(name: String, email: String, password: String): AuthResult = registerResult
    override fun login(email: String, password: String): AuthResult {
        lastLoginEmail = email
        return loginResult
    }
}

/** Doble de prueba de [UserSessionRepository]: guarda en memoria, sin DataStore. */
private class FakeUserSessionRepository : UserSessionRepository {
    override val isReady: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    private val _currentUser = MutableStateFlow(UserProfile(name = "", email = ""))
    override val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    override fun login(profile: UserProfile) {
        _currentUser.value = profile
        _isLoggedIn.value = true
    }

    override fun logout() {
        _isLoggedIn.value = false
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userSessionRepository: FakeUserSessionRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        userSessionRepository = FakeUserSessionRepository()
        viewModel = AuthViewModel(authRepository, userSessionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a successful login opens the session and calls onSuccess`() = runTest {
        val profile = UserProfile(name = "Ada", email = "ada@example.com")
        authRepository.loginResult = AuthResult.Success(profile)

        var onSuccessCalled = false
        viewModel.login("ada@example.com", "pass1234") { onSuccessCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(onSuccessCalled)
        assertTrue(userSessionRepository.isLoggedIn.value)
        assertEquals(profile, userSessionRepository.currentUser.value)
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `a failed login surfaces the error and never opens a session`() = runTest {
        authRepository.loginResult = AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)

        var onSuccessCalled = false
        viewModel.login("ada@example.com", "wrong") { onSuccessCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(onSuccessCalled)
        assertFalse(userSessionRepository.isLoggedIn.value)
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals(AuthFailureReason.INVALID_CREDENTIALS, (state as AuthUiState.Error).reason)
    }

    @Test
    fun `login goes through a Loading state before resolving`() = runTest {
        authRepository.loginResult = AuthResult.Success(UserProfile("Ada", "ada@example.com"))

        viewModel.login("ada@example.com", "pass1234") {}
        // Antes de dejar correr el scheduler, la corrutina ya puso Loading de forma sincronica.
        assertEquals(AuthUiState.Loading, viewModel.uiState.value)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `register with mismatched passwords never reaches the repository`() = runTest {
        viewModel.register("Ada", "ada@example.com", "pass1234", "different") {}
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals(AuthFailureReason.INVALID_INPUT, (state as AuthUiState.Error).reason)
        assertFalse(userSessionRepository.isLoggedIn.value)
    }

    @Test
    fun `a successful register opens the session the same way login does`() = runTest {
        val profile = UserProfile(name = "Ada", email = "ada@example.com")
        authRepository.registerResult = AuthResult.Success(profile)

        viewModel.register("Ada", "ada@example.com", "pass1234", "pass1234") {}
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(userSessionRepository.isLoggedIn.value)
        assertEquals(profile, userSessionRepository.currentUser.value)
    }

    @Test
    fun `clearError resets an Error state back to Idle`() = runTest {
        authRepository.loginResult = AuthResult.Failure(AuthFailureReason.INVALID_CREDENTIALS)
        viewModel.login("ada@example.com", "wrong") {}
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthUiState.Error)

        viewModel.clearError()
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }
}
