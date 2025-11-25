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

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0


        orderRepo = mockk()
        sessionManager = mockk()
        authRepo = mockk()


        viewModel = HistoryViewModel(orderRepo, sessionManager, authRepo)
    }

    @Test
    fun `si es CLIENTE, carga el historial usando su ID de usuario`() = runTest {

        val myUserId = 10L


        val fakeOrders = listOf(mockk<Order>(relaxed = true))


        coEvery { authRepo.getCurrentUserId() } returns myUserId

        coEvery { orderRepo.getOrdersByCustomerId(myUserId) } returns fakeOrders


        viewModel.loadHistory(isAdminView = false)


        val state = viewModel.uiState.value


        assertEquals(null, state.error)

        assertEquals(1, state.orders.size)


        coVerify { orderRepo.getOrdersByCustomerId(myUserId) }

        coVerify(exactly = 0) { orderRepo.getAllOrders() }

        println(" Prueba Cliente: Se cargó solo el historial propio.")
    }

    @Test
    fun `si es ADMIN, carga TODAS las ordenes`() = runTest {

        val adminId = 1L

        val allOrders = listOf(
            mockk<Order>(relaxed = true),
            mockk<Order>(relaxed = true)
        )

        coEvery { authRepo.getCurrentUserId() } returns adminId
        coEvery { orderRepo.getAllOrders() } returns allOrders


        viewModel.loadHistory(isAdminView = true)


        val state = viewModel.uiState.value


        assertEquals(2, state.orders.size)


        coVerify { orderRepo.getAllOrders() }

        println(" Prueba Admin: Se cargó el historial global.")
    }

    @Test
    fun `si el ID local no esta disponible, muestra error y NO llama a la API`() = runTest {

        coEvery { authRepo.getCurrentUserId() } returns null


        viewModel.loadHistory(isAdminView = false)


        val state = viewModel.uiState.value


        assertNotNull(state.error)

        assert(state.error!!.contains("ID de usuario no disponible"))


        assertFalse(state.isLoading)


        coVerify(exactly = 0) { orderRepo.getOrdersByCustomerId(any()) }

        println(" Prueba ID Inválido: Bloqueado correctamente.")
    }

    @Test
    fun `si el repositorio falla por red, muestra el error`() = runTest {

        coEvery { authRepo.getCurrentUserId() } returns 5L

        coEvery { orderRepo.getOrdersByCustomerId(5L) } throws IOException("Sin internet")


        viewModel.loadHistory(false)


        val state = viewModel.uiState.value

        assert(state.error!!.contains("Error de red"))

        println(" Prueba Red: Error de conexión manejado.")
    }
}