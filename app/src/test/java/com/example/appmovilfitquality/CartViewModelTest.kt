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

        viewModel = CartViewModel()
    }

    @Test
    fun `agregar producto con stock suficiente actualiza el total y la lista`() = runBlocking {

        val producto = Product(
            id = 1,
            name = "Mancuernas",
            description = "Test",
            price = 1000.0,
            stock = 10,
            imageUri = null
        )


        val resultado = viewModel.tryAddToCart(producto)




        assertTrue("El resultado debería ser exitoso", resultado.success)


        assertEquals(1, viewModel.cartItems.value.size)


        assertEquals(1000.0, viewModel.cartTotal.value, 0.0)

        println(" Prueba de Agregar: El producto se sumó y el total se actualizó.")
    }

    @Test
    fun `agregar dos veces el mismo producto suma cantidad y duplica precio`() = runBlocking {

        val producto = Product(1, "Cinturón", "Test", 500.0, stock = 5, imageUri = null)


        viewModel.tryAddToCart(producto)
        viewModel.tryAddToCart(producto)

        assertEquals(1, viewModel.cartItems.value.size)


        assertEquals(2, viewModel.cartItems.value[0].quantity)


        assertEquals(1000.0, viewModel.cartTotal.value, 0.0)

        println(" Prueba de Cantidad: Se agruparon los productos correctamente.")
    }

    @Test
    fun `no se puede agregar mas productos que el stock disponible`() = runBlocking {

        val productoLimitado = Product(2, "Escaso", "Test", 100.0, stock = 1, imageUri = null)


        viewModel.tryAddToCart(productoLimitado)
        val resultadoFallo = viewModel.tryAddToCart(productoLimitado)


        assertFalse("Debería fallar por falta de stock", resultadoFallo.success)




        assertEquals(1, viewModel.cartItems.value[0].quantity)

        println(" Prueba de Stock: El sistema bloqueó la venta sin stock.")
    }

    @Test
    fun `limpiar carrito deja todo en cero`() = runBlocking {

        val p = Product(1, "X", "Y", 100.0, 10)
        viewModel.tryAddToCart(p)


        viewModel.clearCart()


        assertEquals(0, viewModel.cartItems.value.size)
        assertEquals(0.0, viewModel.cartTotal.value, 0.0)

        println(" Prueba de Limpiar: Carrito vacío.")
    }
}