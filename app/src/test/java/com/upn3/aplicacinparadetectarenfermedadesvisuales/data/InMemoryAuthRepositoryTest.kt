package com.upn3.aplicacinparadetectarenfermedadesvisuales.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryAuthRepositoryTest {

    private lateinit var repository: InMemoryAuthRepository

    @Before
    fun setUp() {
        repository = InMemoryAuthRepository()
    }

    @Test
    fun `the seeded demo account can log in out of the box`() {
        val result = repository.login(InMemoryAuthRepository.DEMO_EMAIL, InMemoryAuthRepository.DEMO_PASSWORD)
        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun `login fails with a wrong password`() {
        val result = repository.login(InMemoryAuthRepository.DEMO_EMAIL, "wrong-password")
        assertEquals(AuthFailureReason.INVALID_CREDENTIALS, (result as AuthResult.Failure).reason)
    }

    @Test
    fun `login fails for an email that was never registered`() {
        val result = repository.login("nobody@example.com", "whatever123")
        assertEquals(AuthFailureReason.INVALID_CREDENTIALS, (result as AuthResult.Failure).reason)
    }

    @Test
    fun `register then login with the same credentials succeeds`() {
        val register = repository.register("Ada Lovelace", "ada@example.com", "pass1234")
        assertTrue(register is AuthResult.Success)

        val login = repository.login("ada@example.com", "pass1234")
        assertTrue(login is AuthResult.Success)
        assertEquals("Ada Lovelace", (login as AuthResult.Success).profile.name)
    }

    @Test
    fun `email matching is case and whitespace insensitive`() {
        repository.register("Ada Lovelace", "  Ada@Example.com  ", "pass1234")
        val login = repository.login("ada@example.com", "pass1234")
        assertTrue(login is AuthResult.Success)
    }

    @Test
    fun `registering an already-used email fails`() {
        repository.register("First", InMemoryAuthRepository.DEMO_EMAIL, "pass1234")
        val second = repository.register("Second", InMemoryAuthRepository.DEMO_EMAIL, "pass5678")
        assertEquals(AuthFailureReason.EMAIL_ALREADY_REGISTERED, (second as AuthResult.Failure).reason)
    }

    @Test
    fun `registering with a blank name fails validation`() {
        val result = repository.register("   ", "someone@example.com", "pass1234")
        assertEquals(AuthFailureReason.INVALID_INPUT, (result as AuthResult.Failure).reason)
    }

    @Test
    fun `registering with a short password fails validation`() {
        val result = repository.register("Someone", "someone@example.com", "abc")
        assertEquals(AuthFailureReason.INVALID_INPUT, (result as AuthResult.Failure).reason)
    }

    @Test
    fun `a failed registration does not consume the email`() {
        repository.register("", "retry@example.com", "pass1234")
        val retry = repository.register("Now With Name", "retry@example.com", "pass1234")
        assertTrue(retry is AuthResult.Success)
    }
}
