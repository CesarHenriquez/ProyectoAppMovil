package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.OrderRepository.OrderEntity
import com.example.appmovilfitquality.viewmodel.DeliveryViewModel
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class DeliveryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: OrderRepository
    private lateinit var viewModel: DeliveryViewModel

    @Before
    fun setUp() {

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        repo = mockk()

    }

    @Test
    fun `al iniciar, carga la lista de pedidos para delivery`() = runTest {

        val fakeOrders = listOf(
            OrderEntity(id = 1, customerName = "Cliente A", customerEmail = "", customerPhone = "", shippingAddress = "", productSummary = "", totalCLP = 100.0),
            OrderEntity(id = 2, customerName = "Cliente B", customerEmail = "", customerPhone = "", shippingAddress = "", productSummary = "", totalCLP = 200.0)
        )


        coEvery { repo.getOrdersForDelivery() } returns fakeOrders


        viewModel = DeliveryViewModel(repo)


        val state = viewModel.uiState.value


        assertFalse(state.isLoading)


        assertEquals(2, state.orders.size)
        assertEquals("Cliente A", state.orders[0].customerName)

        println(" Prueba Carga Delivery: Lista obtenida correctamente.")
    }

    @Test
    fun `si falla la carga de pedidos, muestra error`() = runTest {

        coEvery { repo.getOrdersForDelivery() } throws IOException("Servidor caído")


        viewModel = DeliveryViewModel(repo)


        val state = viewModel.uiState.value
        assertFalse(state.isLoading)

        assert(state.error!!.contains("Error de red"))

        println(" Prueba Error Delivery: Fallo de red manejado.")
    }

    @Test
    fun `subir comprobante exitoso actualiza la lista y muestra mensaje`() = runTest {

        coEvery { repo.getOrdersForDelivery() } returns emptyList()


        coEvery { repo.saveDeliveryProof(any(), any()) } returns Unit

        viewModel = DeliveryViewModel(repo) // Init llama a refresh()


        viewModel.submitProof(orderId = 100, proofUri = "foto.jpg")


        val state = viewModel.uiState.value


        coVerify(exactly = 1) { repo.saveDeliveryProof(100, "foto.jpg") }


        coVerify(exactly = 2) { repo.getOrdersForDelivery() }



        println(" Prueba Comprobante: Subida y recarga exitosas.")
    }
}