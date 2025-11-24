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

    // Mocks de todas las dependencias
    private lateinit var orderRepo: OrderRepository
    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var productRepo: ProductRepository

    private lateinit var viewModel: CheckoutViewModel

    @Before
    fun setUp() {
        // 1. Mockear Log para que no falle
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        // 2. Inicializar mocks
        orderRepo = mockk()
        authRepo = mockk()
        sessionManager = mockk()
        productRepo = mockk()

        // 3. Inicializar ViewModel
        viewModel = CheckoutViewModel(orderRepo, authRepo, sessionManager, productRepo)
    }

    @Test
    fun `si el carrito esta vacio, muestra error y no llama al repositorio`() = runTest {
        // --- 1. EJECUCIÓN ---
        viewModel.placeOrderFromCart(emptyList(), "Calle Falsa 123")

        // --- 2. VERIFICACIÓN ---
        val state = viewModel.ui.value

        // Debe mostrar error
        assertEquals("El carrito está vacío.", state.error)
        // No debió intentar cargar
        assertFalse(state.isPlacing)

        // Verificamos que NUNCA se llamó a placeOrder en el repositorio
        coVerify(exactly = 0) { orderRepo.placeOrder(any(), any(), any()) }

        println("✅ Prueba Carrito Vacío: Bloqueado correctamente.")
    }

    @Test
    fun `si falta la direccion, muestra error`() = runTest {
        // --- 1. PREPARACIÓN ---
        val items = listOf(CartItem(Product(1, "X", "Y", 10.0, 1), 1))

        // --- 2. EJECUCIÓN ---
        // Pasamos dirección vacía ""
        viewModel.placeOrderFromCart(items, "")

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.ui.value
        assertEquals("Debe ingresar una dirección de envío.", state.error)

        println("✅ Prueba Dirección: Bloqueado por falta de dirección.")
    }

    @Test
    fun `compra exitosa cambia el estado a success`() = runTest {
        // --- 1. PREPARACIÓN ---
        val email = "cliente@test.com"
        val direccion = "Av. Siempre Viva 742"
        val items = listOf(CartItem(Product(1, "Pesas", "Gym", 5000.0, 5), 2))

        // Simulamos que hay un email en sesión
        every { sessionManager.emailFlow } returns flowOf(email)

        // Simulamos que el repositorio hace la compra sin error (devuelve cualquier cosa, ej: mock order)
        coEvery { orderRepo.placeOrder(email, direccion, items) } returns mockk()

        // --- 2. EJECUCIÓN ---
        viewModel.placeOrderFromCart(items, direccion)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.ui.value

        // A) Debe ser exitoso
        assertTrue(state.success)
        // B) No debe haber error
        assertEquals(null, state.error)

        // C) Verificamos que SÍ se llamó al repositorio con los datos correctos
        coVerify { orderRepo.placeOrder(email, direccion, items) }

        println("✅ Prueba Compra Exitosa: Flujo completo verificado.")
    }
}