package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.ProductRepository
import com.example.appmovilfitquality.domain.model.CartItem
import com.example.appmovilfitquality.domain.model.Product
import com.example.appmovilfitquality.viewmodel.CheckoutViewModel
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()


    private lateinit var orderRepo: OrderRepository
    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var productRepo: ProductRepository

    private lateinit var viewModel: CheckoutViewModel

    @Before
    fun setUp() {

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0


        orderRepo = mockk()
        authRepo = mockk()
        sessionManager = mockk()
        productRepo = mockk()


        viewModel = CheckoutViewModel(orderRepo, authRepo, sessionManager, productRepo)
    }

    @Test
    fun `si el carrito esta vacio, muestra error y no llama al repositorio`() = runTest {

        viewModel.placeOrderFromCart(emptyList(), "Calle Falsa 123")


        val state = viewModel.ui.value


        assertEquals("El carrito está vacío.", state.error)

        assertFalse(state.isPlacing)


        coVerify(exactly = 0) { orderRepo.placeOrder(any(), any(), any()) }

        println(" Prueba Carrito Vacío: Bloqueado correctamente.")
    }

    @Test
    fun `si falta la direccion, muestra error`() = runTest {

        val items = listOf(CartItem(Product(1, "X", "Y", 10.0, 1), 1))



        viewModel.placeOrderFromCart(items, "")


        val state = viewModel.ui.value
        assertEquals("Debe ingresar una dirección de envío.", state.error)

        println(" Prueba Dirección: Bloqueado por falta de dirección.")
    }

    @Test
    fun `compra exitosa cambia el estado a success`() = runTest {

        val email = "cliente@test.com"
        val direccion = "Av. Siempre Viva 742"
        val items = listOf(CartItem(Product(1, "Pesas", "Gym", 5000.0, 5), 2))


        every { sessionManager.emailFlow } returns flowOf(email)


        coEvery { orderRepo.placeOrder(email, direccion, items) } returns mockk()


        viewModel.placeOrderFromCart(items, direccion)


        val state = viewModel.ui.value


        assertTrue(state.success)

        assertEquals(null, state.error)


        coVerify { orderRepo.placeOrder(email, direccion, items) }

        println(" Prueba Compra Exitosa: Flujo completo verificado.")
    }
}