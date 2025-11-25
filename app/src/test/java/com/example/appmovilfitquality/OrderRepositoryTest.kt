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

        orderRepository = OrderRepository(apiService, authRepository)
    }

    @Test
    fun `getOrdersByCustomerId convierte correctamente la fecha String a Long`() = runBlocking {

        val fechaTexto = "2025-11-24"


        val fakeDto = OrderDto(
            id = 100,
            customerId = 1L,
            shippingAddressId = 1L,
            fecha = fechaTexto,
            items = emptyList(),
            shippingAddressDetail = 55L,
            totalCLP = 10000.0
        )


        coEvery { apiService.getOrdersByCustomer(url = any()) } returns listOf(fakeDto)


        val resultList = orderRepository.getOrdersByCustomerId(1L)
        val ordenDominio = resultList.first()




        assertEquals(100, ordenDominio.id)


        assertTrue("El timestamp debe ser válido y mayor a 0", ordenDominio.timestamp > 0)


        assertEquals("Dirección ID: 55", ordenDominio.shippingAddress)

        println("✅ Prueba de Fecha: El repositorio convirtió '$fechaTexto' a ${ordenDominio.timestamp} ms correctamente.")
    }
}