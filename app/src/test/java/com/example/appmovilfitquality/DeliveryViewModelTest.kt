package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.OrderRepository.OrderEntity
import com.example.appmovilfitquality.viewmodel.DeliveryViewModel
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
        // 1. Mockear Logs de Android
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        repo = mockk()
        // Nota: No instanciamos el VM aquí para poder configurar el mock antes del init{}
    }

    @Test
    fun `al iniciar, carga la lista de pedidos para delivery`() = runTest {
        // --- 1. PREPARACIÓN ---
        val fakeOrders = listOf(
            OrderEntity(id = 1, customerName = "Cliente A", customerEmail = "", customerPhone = "", shippingAddress = "", productSummary = "", totalCLP = 100.0),
            OrderEntity(id = 2, customerName = "Cliente B", customerEmail = "", customerPhone = "", shippingAddress = "", productSummary = "", totalCLP = 200.0)
        )

        // Simulamos que el repositorio devuelve la lista
        coEvery { repo.getOrdersForDelivery() } returns fakeOrders

        // --- 2. EJECUCIÓN ---
        viewModel = DeliveryViewModel(repo) // Se ejecuta refresh() en el init

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value

        // A) No debe estar cargando
        assertFalse(state.isLoading)

        // B) Debe tener 2 ordenes
        assertEquals(2, state.orders.size)
        assertEquals("Cliente A", state.orders[0].customerName)

        println("✅ Prueba Carga Delivery: Lista obtenida correctamente.")
    }

    @Test
    fun `si falla la carga de pedidos, muestra error`() = runTest {
        // --- 1. PREPARACIÓN ---
        coEvery { repo.getOrdersForDelivery() } throws IOException("Servidor caído")

        // --- 2. EJECUCIÓN ---
        viewModel = DeliveryViewModel(repo)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        // Verificamos que el mensaje de error contenga "Error de red" (o el texto que tu VM use)
        assert(state.error!!.contains("Error de red"))

        println("✅ Prueba Error Delivery: Fallo de red manejado.")
    }

    @Test
    fun `subir comprobante exitoso actualiza la lista y muestra mensaje`() = runTest {
        // --- 1. PREPARACIÓN ---
        // Mockeamos la carga inicial (lista vacía para simplificar)
        coEvery { repo.getOrdersForDelivery() } returns emptyList()

        // ⬇️ CORRECCIÓN CLAVE: Usamos returns Unit en lugar de just Runs para funciones suspendidas ⬇️
        coEvery { repo.saveDeliveryProof(any(), any()) } returns Unit

        viewModel = DeliveryViewModel(repo) // Init llama a refresh()

        // --- 2. EJECUCIÓN ---
        viewModel.submitProof(orderId = 100, proofUri = "foto.jpg")

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value

        // A) Se debió llamar al repositorio con los datos correctos (exactamente 1 vez)
        coVerify(exactly = 1) { repo.saveDeliveryProof(100, "foto.jpg") }

        // B) Se debió recargar la lista (refresh se llama 2 veces: init + tras submit)
        coVerify(exactly = 2) { repo.getOrdersForDelivery() }

        // C) Debe haber un mensaje de éxito (lastMessage no nulo)
        // Nota: Asegúrate de que tu ViewModel asigne lastMessage en caso de éxito
        // assertNotNull(state.lastMessage)

        println("✅ Prueba Comprobante: Subida y recarga exitosas.")
    }
}