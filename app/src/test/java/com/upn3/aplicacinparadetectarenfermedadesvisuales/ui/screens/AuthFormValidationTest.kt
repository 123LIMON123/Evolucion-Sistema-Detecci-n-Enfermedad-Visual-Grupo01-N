package com.upn3.aplicacinparadetectarenfermedadesvisuales.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFormValidationTest {

    @Test
    fun `accepts a plain valid email`() {
        assertTrue(isValidEmailFormat("demo@ocucheck.ai"))
    }

    @Test
    fun `accepts an email with leading or trailing whitespace`() {
        assertTrue(isValidEmailFormat("  demo@ocucheck.ai  "))
    }

    @Test
    fun `rejects an email missing the at sign`() {
        assertFalse(isValidEmailFormat("demoocucheck.ai"))
    }

    @Test
    fun `rejects an email missing a domain extension`() {
        assertFalse(isValidEmailFormat("demo@ocucheck"))
    }

    @Test
    fun `rejects an email with a space in the middle`() {
        assertFalse(isValidEmailFormat("de mo@ocucheck.ai"))
    }

    @Test
    fun `rejects a blank string`() {
        assertFalse(isValidEmailFormat(""))
    }

    @Test
    fun `rejects an email with no local part`() {
        assertFalse(isValidEmailFormat("@ocucheck.ai"))
    }
}
