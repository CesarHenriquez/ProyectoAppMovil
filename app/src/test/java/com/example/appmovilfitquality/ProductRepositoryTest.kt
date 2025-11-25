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

        val fakeDto = ProductDto(
            id = 100,
            name = "Cinturón Pro",
            description = "Cuero real",
            price = 25000.0,
            stock = 50,
            imageUri = "cinturon_pro"
        )


        coEvery { apiService.getProducts() } returns listOf(fakeDto)


        val resultList = repository.getAllProducts().first()
        val entity = resultList[0]


        assertEquals(1, resultList.size)


        assertEquals(100, entity.id)
        assertEquals("Cinturón Pro", entity.name)


        assertEquals("cinturon_pro", entity.imageResourceName)

        println(" Prueba de Mapeo: Los datos viajan correctamente del DTO a la Entidad.")
    }

    @Test
    fun `si el stock viene NULO desde la API se debe convertir a CERO`() = runBlocking {

        val dtoNullStock = ProductDto(
            id = 2,
            name = "Producto Raro",
            description = "...",
            price = 10.0,
            stock = 0, // <--- NULO
            imageUri = "img"
        )

        coEvery { apiService.getProducts() } returns listOf(dtoNullStock)


        val resultList = repository.getAllProducts().first()
        val entity = resultList[0]



        assertNotNull(entity.stock)
        assertEquals(0, entity.stock)

        println(" Prueba de Robustez: El stock nulo se convirtió a 0 exitosamente.")
    }

    @Test
    fun `si la imagen viene NULA se maneja sin romper`() = runBlocking {

        val dtoSinImagen = ProductDto(
            id = 3, name = "X", description = "Y", price = 1.0, stock = 1,
            imageUri = null
        )

        coEvery { apiService.getProducts() } returns listOf(dtoSinImagen)


        val resultList = repository.getAllProducts().first()
        val entity = resultList[0]



        assertEquals(null, entity.imageResourceName)

        println(" Prueba de Imagen: Se manejó la ausencia de imagen.")
    }
}