package com.example.appmovilfitquality

import com.example.appmovilfitquality.domain.model.Product
import com.example.appmovilfitquality.viewmodel.CartViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartViewModelTest {

    private lateinit var viewModel: CartViewModel

    @Before
    fun setUp() {
        // Inicializamos el ViewModel antes de cada test para que el carrito esté vacío
        viewModel = CartViewModel()
    }

    @Test
    fun `agregar producto con stock suficiente actualiza el total y la lista`() = runBlocking {
        // --- 1. PREPARACIÓN (Given) ---
        // Creamos un producto falso con Stock 10 y Precio 1000
        val producto = Product(
            id = 1,
            name = "Mancuernas",
            description = "Test",
            price = 1000.0,
            stock = 10,
            imageUri = null
        )

        // --- 2. EJECUCIÓN (When) ---
        val resultado = viewModel.tryAddToCart(producto)

        // --- 3. VERIFICACIÓN (Then) ---

        // A) La operación debe ser exitosa
        assertTrue("El resultado debería ser exitoso", resultado.success)

        // B) El carrito debe tener 1 elemento
        assertEquals(1, viewModel.cartItems.value.size)

        // C) El total debe ser 1000.0
        assertEquals(1000.0, viewModel.cartTotal.value, 0.0)

        println("✅ Prueba de Agregar: El producto se sumó y el total se actualizó.")
    }

    @Test
    fun `agregar dos veces el mismo producto suma cantidad y duplica precio`() = runBlocking {
        // --- 1. PREPARACIÓN ---
        val producto = Product(1, "Cinturón", "Test", 500.0, stock = 5, imageUri = null)

        // --- 2. EJECUCIÓN ---
        viewModel.tryAddToCart(producto) // Agregamos el primero
        viewModel.tryAddToCart(producto) // Agregamos el segundo

        // --- 3. VERIFICACIÓN ---
        // Debería haber solo 1 item en la lista (pero con cantidad 2)
        assertEquals(1, viewModel.cartItems.value.size)

        // La cantidad de ese item debe ser 2
        assertEquals(2, viewModel.cartItems.value[0].quantity)

        // El total debe ser 1000.0 (500 * 2)
        assertEquals(1000.0, viewModel.cartTotal.value, 0.0)

        println("✅ Prueba de Cantidad: Se agruparon los productos correctamente.")
    }

    @Test
    fun `no se puede agregar mas productos que el stock disponible`() = runBlocking {
        // --- 1. PREPARACIÓN ---
        // Producto con POCO stock (solo 1)
        val productoLimitado = Product(2, "Escaso", "Test", 100.0, stock = 1, imageUri = null)

        // --- 2. EJECUCIÓN ---
        viewModel.tryAddToCart(productoLimitado) // Primero: OK (Stock baja virtualmente a 0)
        val resultadoFallo = viewModel.tryAddToCart(productoLimitado) // Segundo: DEBERÍA FALLAR

        // --- 3. VERIFICACIÓN ---
        // A) El segundo intento debe decir que falló
        assertFalse("Debería fallar por falta de stock", resultadoFallo.success)

        // B) El mensaje de error debe ser claro
        // (Opcional: verifica si el mensaje contiene "Stock insuficiente")
        // assertTrue(resultadoFallo.message?.contains("Stock insuficiente") == true)

        // C) El carrito solo debe tener 1 unidad, no 2
        assertEquals(1, viewModel.cartItems.value[0].quantity)

        println("✅ Prueba de Stock: El sistema bloqueó la venta sin stock.")
    }

    @Test
    fun `limpiar carrito deja todo en cero`() = runBlocking {
        // --- 1. PREPARACIÓN ---
        val p = Product(1, "X", "Y", 100.0, 10)
        viewModel.tryAddToCart(p)

        // --- 2. EJECUCIÓN ---
        viewModel.clearCart()

        // --- 3. VERIFICACIÓN ---
        assertEquals(0, viewModel.cartItems.value.size)
        assertEquals(0.0, viewModel.cartTotal.value, 0.0)

        println("✅ Prueba de Limpiar: Carrito vacío.")
    }
}