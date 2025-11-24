package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.repository.ProductRepository
import com.example.appmovilfitquality.data.repository.ProductRepository.ProductEntity
import com.example.appmovilfitquality.viewmodel.StoreViewModel
import io.mockk.coEvery
import io.mockk.every // Importante para mockear Log
import io.mockk.mockk
import io.mockk.mockkStatic // Importante para mockear Log
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

    // Regla necesaria para probar ViewModels que usan viewModelScope
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ProductRepository
    private lateinit var viewModel: StoreViewModel

    @Before
    fun setUp() {
        // ⬇️ SOLUCIÓN AL ERROR DE LOGS ⬇️
        // Simulamos la clase estática Log de Android para que no falle en la JVM local
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        // ⬆️ ------------------------- ⬆️

        repository = mockk()
    }

    @Test
    fun `al iniciar, carga productos exitosamente y actualiza el estado`() = runTest {
        // --- 1. PREPARACIÓN ---
        val fakeEntity = ProductEntity(
            id = 1,
            name = "Cinturón",
            description = "Gym",
            price = 20000.0,
            stock = 5,
            imageUri = "cinturon_fit",
            imageResourceName = "cinturon_fit"
        )

        // Simulamos que el repositorio devuelve un Flow con una lista
        coEvery { repository.getAllProducts() } returns flowOf(listOf(fakeEntity))

        // --- 2. EJECUCIÓN ---
        // Al instanciar el ViewModel, se ejecuta init { loadProducts() }
        viewModel = StoreViewModel(repository)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value

        // A) No debe estar cargando
        assertFalse(state.isLoading)

        // B) No debe haber error
        assertEquals(null, state.error)

        // C) La lista debe tener 1 producto
        assertEquals(1, state.products.size)

        // D) Verificamos que el mapeo a Dominio mantuvo los datos
        assertEquals("Cinturón", state.products[0].name)

        println("✅ Prueba de Carga Exitosa: Pasó correctamente.")
    }

    @Test
    fun `si el repositorio lanza error de red, el estado muestra el mensaje de error`() = runTest {
        // --- 1. PREPARACIÓN ---
        // Simulamos que el repositorio lanza una excepción al intentar leer el flow
        coEvery { repository.getAllProducts() } returns flow {
            throw IOException("Fallo de conexión")
        }

        // --- 2. EJECUCIÓN ---
        viewModel = StoreViewModel(repository)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value

        // A) La lista debe estar vacía
        assertEquals(0, state.products.size)

        // B) El campo error NO debe ser nulo
        assertNotNull(state.error)

        // C) Verificamos que el ViewModel capturó la excepción y puso el mensaje correcto
        // (Este mensaje está definido dentro de tu StoreViewModel)
        assertEquals("Error de Red: Verifique conexión al microservicio.", state.error)

        println("✅ Prueba de Error: El ViewModel manejó la excepción correctamente.")
    }
}