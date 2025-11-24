package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.domain.model.Order
import com.example.appmovilfitquality.viewmodel.HistoryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var orderRepo: OrderRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepo: AuthRepository
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        // 1. Mockear Log de Android para evitar "Method not mocked"
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        // 2. Mocks de dependencias
        orderRepo = mockk()
        sessionManager = mockk()
        authRepo = mockk()

        // Instancia del VM
        viewModel = HistoryViewModel(orderRepo, sessionManager, authRepo)
    }

    @Test
    fun `si es CLIENTE, carga el historial usando su ID de usuario`() = runTest {
        // --- 1. PREPARACIÓN ---
        val myUserId = 10L

        // ⬇️ CORRECCIÓN: Usamos relaxed = true para que no falle al leer propiedades (id, total) en los logs
        val fakeOrders = listOf(mockk<Order>(relaxed = true))

        // Simulamos que la sesión devuelve un ID válido
        coEvery { authRepo.getCurrentUserId() } returns myUserId
        // Simulamos que el repo devuelve una lista
        coEvery { orderRepo.getOrdersByCustomerId(myUserId) } returns fakeOrders

        // --- 2. EJECUCIÓN ---
        // false = Vista de Cliente
        viewModel.loadHistory(isAdminView = false)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value

        // A) No debe haber error
        assertEquals(null, state.error)
        // B) Debe tener la lista cargada
        assertEquals(1, state.orders.size)

        // C) Verificamos que llamó al método de CLIENTE
        coVerify { orderRepo.getOrdersByCustomerId(myUserId) }
        // Confirmamos que NO llamó al de todos
        coVerify(exactly = 0) { orderRepo.getAllOrders() }

        println("✅ Prueba Cliente: Se cargó solo el historial propio.")
    }

    @Test
    fun `si es ADMIN, carga TODAS las ordenes`() = runTest {
        // --- 1. PREPARACIÓN ---
        val adminId = 1L
        // ⬇️ CORRECCIÓN: relaxed = true aquí también
        val allOrders = listOf(
            mockk<Order>(relaxed = true),
            mockk<Order>(relaxed = true)
        )

        coEvery { authRepo.getCurrentUserId() } returns adminId
        coEvery { orderRepo.getAllOrders() } returns allOrders

        // --- 2. EJECUCIÓN ---
        // true = Vista de Admin
        viewModel.loadHistory(isAdminView = true)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value

        // A) Debe tener 2 ordenes
        assertEquals(2, state.orders.size)

        // B) Verificamos que llamó al método getAllOrders
        coVerify { orderRepo.getAllOrders() }

        println("✅ Prueba Admin: Se cargó el historial global.")
    }

    @Test
    fun `si el ID local no esta disponible, muestra error y NO llama a la API`() = runTest {
        // --- 1. PREPARACIÓN ---
        // Simulamos el caso de ID nulo o cero
        coEvery { authRepo.getCurrentUserId() } returns null

        // --- 2. EJECUCIÓN ---
        viewModel.loadHistory(isAdminView = false)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value

        // A) Debe mostrar mensaje de error
        assertNotNull(state.error)
        // Verificamos parte del texto del error
        assert(state.error!!.contains("ID de usuario no disponible"))

        // B) No debe estar cargando
        assertFalse(state.isLoading)

        // C) CRÍTICO: Aseguramos que NO intentó llamar a la API
        coVerify(exactly = 0) { orderRepo.getOrdersByCustomerId(any()) }

        println("✅ Prueba ID Inválido: Bloqueado correctamente.")
    }

    @Test
    fun `si el repositorio falla por red, muestra el error`() = runTest {
        // --- 1. PREPARACIÓN ---
        coEvery { authRepo.getCurrentUserId() } returns 5L
        // Simulamos fallo de red
        coEvery { orderRepo.getOrdersByCustomerId(5L) } throws IOException("Sin internet")

        // --- 2. EJECUCIÓN ---
        viewModel.loadHistory(false)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value
        // Verifica que capturó la excepción IOException y puso el mensaje correcto
        assert(state.error!!.contains("Error de red"))

        println("✅ Prueba Red: Error de conexión manejado.")
    }
}