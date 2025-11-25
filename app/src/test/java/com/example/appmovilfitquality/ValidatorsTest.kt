package com.example.appmovilfitquality

import com.example.appmovilfitquality.domain.validation.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    // --- Pruebas de Email ---
    @Test
    fun `validateEmail retorna nulo (exito) con email valido`() {
        val result = Validators.validateEmail("usuario@test.com")
        assertNull(result) // Null significa que no hubo error
    }

    @Test
    fun `validateEmail retorna error si falta el arroba`() {
        val result = Validators.validateEmail("usuariotest.com")
        assertEquals("Formato de email inválido (ej: usuario@dominio.com)", result)
    }

    // --- Pruebas de Password ---
    @Test
    fun `validatePassword retorna nulo con clave segura`() {

        val result = Validators.validatePassword("FitQuality2025")
        assertNull(result)
    }

    @Test
    fun `validatePassword retorna error si es muy corta`() {
        val result = Validators.validatePassword("Ab1")

        assert(result != null)
    }

    @Test
    fun `validatePassword retorna error si no tiene numeros`() {
        val result = Validators.validatePassword("SoloLetrasMayus")
        assert(result != null)
    }

    // --- Pruebas de Telefono ---
    @Test
    fun `validatePhone acepta numeros de 8 o 9 digitos`() {
        assertNull(Validators.validatePhone("912345678"))
        assertNull(Validators.validatePhone("12345678"))
    }

    @Test
    fun `validatePhone rechaza letras`() {
        val result = Validators.validatePhone("91234567a")
        assertEquals("Solo números (8 o 9 dígitos)", result)
    }
}