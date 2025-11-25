package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.dto.LoginResponseDto
import com.example.appmovilfitquality.data.dto.RolDto
import com.example.appmovilfitquality.data.dto.UserDto
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.remote.ApiService
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    // Mocks
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    // Clase a probar
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        apiService = mockk()

        sessionManager = mockk(relaxed = true)

        authRepository = AuthRepository(apiService, sessionManager)
    }

    @Test
    fun `login con rol ADMINISTRADOR debe mapear correctamente a UserRole STOCK`() = runBlocking {
        // --- ARRANGE ---
        val email = "admin@bootstrap.com"
        val password = "123"

        val fakeUserDto = UserDto(
            id = 1,
            name = "Admin Test",
            email = email,

            rol = RolDto(id = 1, nombre = "ADMINISTRADOR")
        )
        val fakeResponse = LoginResponseDto(token = "token_admin", user = fakeUserDto)


        coEvery {
            apiService.login(url = any(), credentials = any())
        } returns fakeResponse


        val result = authRepository.login(email, password)



        assertEquals(UserRole.STOCK, result.role)


        coVerify {
            sessionManager.saveSession(
                userId = 1,
                email = email,
                role = UserRole.STOCK,
                token = "token_admin"
            )
        }
        println(" Test Admin OK")
    }

    @Test
    fun `login con rol CLIENTE se mantiene como CLIENTE`() = runBlocking {

        val email = "cliente@test.com"

        val fakeUserDto = UserDto(
            id = 2,
            name = "Cliente Test",
            email = email,
            rol = RolDto(id = 2, nombre = "CLIENTE")
        )
        val fakeResponse = LoginResponseDto(token = "token_cliente", user = fakeUserDto)


        coEvery { apiService.login(any(), any()) } returns fakeResponse


        val result = authRepository.login(email, "pass")


        assertEquals(UserRole.CLIENTE, result.role)

        coVerify {
            sessionManager.saveSession(userId = 2, email = email, role = UserRole.CLIENTE, token = "token_cliente")
        }
        println(" Test Cliente OK")
    }

    @Test
    fun `login con rol DELIVERY se mantiene como DELIVERY`() = runBlocking {

        val email = "delivery@test.com"
        val fakeUserDto = UserDto(
            id = 3,
            name = "Repartidor",
            email = email,
            rol = RolDto(id = 3, nombre = "DELIVERY")
        )
        val fakeResponse = LoginResponseDto(token = "token_delivery", user = fakeUserDto)


        coEvery { apiService.login(any(), any()) } returns fakeResponse


        val result = authRepository.login(email, "pass")


        assertEquals(UserRole.DELIVERY, result.role)

        println(" Test Delivery OK")
    }
}