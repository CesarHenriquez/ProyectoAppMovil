package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.repository.ProductRepository
import com.example.appmovilfitquality.data.repository.ProductRepository.ProductEntity
import com.example.appmovilfitquality.viewmodel.StoreViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class StoreViewModelTest {


    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ProductRepository
    private lateinit var viewModel: StoreViewModel

    @Before
    fun setUp() {

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0


        repository = mockk()
    }

    @Test
    fun `al iniciar, carga productos exitosamente y actualiza el estado`() = runTest {

        val fakeEntity = ProductEntity(
            id = 1,
            name = "Cinturón",
            description = "Gym",
            price = 20000.0,
            stock = 5,
            imageUri = "cinturon_fit",
            imageResourceName = "cinturon_fit"
        )


        coEvery { repository.getAllProducts() } returns flowOf(listOf(fakeEntity))


        viewModel = StoreViewModel(repository)


        val state = viewModel.uiState.value


        assertFalse(state.isLoading)


        assertEquals(null, state.error)


        assertEquals(1, state.products.size)


        assertEquals("Cinturón", state.products[0].name)

        println(" Prueba de Carga Exitosa: Pasó correctamente.")
    }

    @Test
    fun `si el repositorio lanza error de red, el estado muestra el mensaje de error`() = runTest {

        coEvery { repository.getAllProducts() } returns flow {
            throw IOException("Fallo de conexión")
        }


        viewModel = StoreViewModel(repository)


        val state = viewModel.uiState.value


        assertEquals(0, state.products.size)


        assertNotNull(state.error)


        assertEquals("Error de Red: Verifique conexión al microservicio.", state.error)

        println(" Prueba de Error: El ViewModel manejó la excepción correctamente.")
    }
}