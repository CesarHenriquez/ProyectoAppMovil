package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.dto.OrderDto
import com.example.appmovilfitquality.data.remote.ApiService
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OrderRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var authRepository: AuthRepository
    private lateinit var orderRepository: OrderRepository

    @Before
    fun setUp() {
        apiService = mockk()
        authRepository = mockk()
        // Instanciamos el repositorio real con los mocks
        orderRepository = OrderRepository(apiService, authRepository)
    }

    @Test
    fun `getOrdersByCustomerId convierte correctamente la fecha String a Long`() = runBlocking {
        // --- 1. PREPARACIÓN ---
        val fechaTexto = "2025-11-24"

        // Simulamos el DTO que llega del servidor (con la fecha como String)
        val fakeDto = OrderDto(
            id = 100,
            customerId = 1L,
            shippingAddressId = 1L,
            fecha = fechaTexto, // <--- AQUÍ ESTÁ LA CLAVE: Llega como String
            items = emptyList(),
            shippingAddressDetail = 55L, // Simulamos el ID de dirección
            totalCLP = 10000.0
        )

        // Configuramos el mock para devolver este DTO
        // Usamos any() para la URL porque la construimos dinámicamente
        coEvery { apiService.getOrdersByCustomer(url = any()) } returns listOf(fakeDto)

        // --- 2. EJECUCIÓN ---
        val resultList = orderRepository.getOrdersByCustomerId(1L)
        val ordenDominio = resultList.first()

        // --- 3. VERIFICACIÓN ---

        // A) Verificamos que el ID se mantuvo
        assertEquals(100, ordenDominio.id)

        // B) VERIFICACIÓN CRÍTICA: La fecha (timestamp)
        // El repositorio debió convertir "2025-11-24" a milisegundos.
        // Ese timestamp debe ser mayor a 0.
        assertTrue("El timestamp debe ser válido y mayor a 0", ordenDominio.timestamp > 0)

        // C) Verificamos que los campos "Auxiliares" se mapearon a Strings legibles
        // En tu código pusimos: "Dirección ID: {shippingAddressDetail}"
        assertEquals("Dirección ID: 55", ordenDominio.shippingAddress)

        println("✅ Prueba de Fecha: El repositorio convirtió '$fechaTexto' a ${ordenDominio.timestamp} ms correctamente.")
    }
}