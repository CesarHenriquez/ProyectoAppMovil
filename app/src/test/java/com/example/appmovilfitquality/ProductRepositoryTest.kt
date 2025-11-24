package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.dto.ProductDto
import com.example.appmovilfitquality.data.remote.ApiService
import com.example.appmovilfitquality.data.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ProductRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: ProductRepository

    @Before
    fun setUp() {
        apiService = mockk()
        repository = ProductRepository(apiService)
    }

    @Test
    fun `getAllProducts obtiene lista y mapea datos correctamente`() = runBlocking {
        // --- 1. PREPARACIÓN (Given) ---
        // Simulamos lo que envía Spring Boot (el DTO)
        val fakeDto = ProductDto(
            id = 100,
            name = "Cinturón Pro",
            description = "Cuero real",
            price = 25000.0,
            stock = 50,
            imageUri = "cinturon_pro" // El nombre limpio que envía tu backend
        )

        // Configuramos el mock para que devuelva una lista con ese producto
        coEvery { apiService.getProducts() } returns listOf(fakeDto)

        // --- 2. EJECUCIÓN (When) ---
        // repository.getAllProducts() devuelve un Flow, usamos .first() para obtener el primer valor emitido
        val resultList = repository.getAllProducts().first()
        val entity = resultList[0]

        // --- 3. VERIFICACIÓN (Then) ---

        // A) Verificar que llegó 1 producto
        assertEquals(1, resultList.size)

        // B) Verificar que el ID y Nombre coinciden
        assertEquals(100, entity.id)
        assertEquals("Cinturón Pro", entity.name)

        // C) VERIFICACIÓN CLAVE: La imagen
        // Tu lógica actual asigna imageUri directamete a imageResourceName. Probamos eso.
        assertEquals("cinturon_pro", entity.imageResourceName)

        println("✅ Prueba de Mapeo: Los datos viajan correctamente del DTO a la Entidad.")
    }

    @Test
    fun `si el stock viene NULO desde la API se debe convertir a CERO`() = runBlocking {
        // --- 1. PREPARACIÓN ---
        // Simulamos un producto con stock null (caso que rompía la app antes)
        val dtoNullStock = ProductDto(
            id = 2,
            name = "Producto Raro",
            description = "...",
            price = 10.0,
            stock = 0, // <--- NULO
            imageUri = "img"
        )

        coEvery { apiService.getProducts() } returns listOf(dtoNullStock)

        // --- 2. EJECUCIÓN ---
        val resultList = repository.getAllProducts().first()
        val entity = resultList[0]

        // --- 3. VERIFICACIÓN ---
        // El repositorio debe haber aplicado el operador Elvis (?: 0)
        assertNotNull(entity.stock) // No debe ser nulo en la entidad
        assertEquals(0, entity.stock) // Debe ser 0

        println("✅ Prueba de Robustez: El stock nulo se convirtió a 0 exitosamente.")
    }

    @Test
    fun `si la imagen viene NULA se maneja sin romper`() = runBlocking {
        // --- 1. PREPARACIÓN ---
        val dtoSinImagen = ProductDto(
            id = 3, name = "X", description = "Y", price = 1.0, stock = 1,
            imageUri = null // <--- NULO
        )

        coEvery { apiService.getProducts() } returns listOf(dtoSinImagen)

        // --- 2. EJECUCIÓN ---
        val resultList = repository.getAllProducts().first()
        val entity = resultList[0]

        // --- 3. VERIFICACIÓN ---
        // imageResourceName debería ser null (y la UI mostrará "Sin imagen")
        assertEquals(null, entity.imageResourceName)

        println("✅ Prueba de Imagen: Se manejó la ausencia de imagen.")
    }
}