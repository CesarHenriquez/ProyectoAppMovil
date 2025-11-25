package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.AuthRepository.User
import com.example.appmovilfitquality.domain.model.UserRole
import com.example.appmovilfitquality.viewmodel.ProfileViewModel
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: AuthRepository
    private lateinit var session: SessionManager
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        repo = mockk()
        session = mockk()
    }

    @Test
    fun `al iniciar, carga el perfil del usuario desde el email de sesion`() = runTest {

        val email = "user@test.com"
        val fakeUser = User(
            name = "Usuario Test",
            email = email,
            phone = "123456",
            password = "",
            role = UserRole.CLIENTE
        )


        every { session.emailFlow } returns flowOf(email)

        coEvery { repo.getUserByEmail(email) } returns fakeUser


        viewModel = ProfileViewModel(repo, session)


        val state = viewModel.uiState.value


        assertFalse(state.isLoading)


        assertEquals("Usuario Test", state.name)
        assertEquals("123456", state.phone)
        assertEquals("CLIENTE", state.role)

        println(" Prueba Carga Perfil: Datos obtenidos correctamente.")
    }

    @Test
    fun `cambiar contraseña falla si la confirmacion no coincide`() = runTest {

        every { session.emailFlow } returns flowOf("")
        viewModel = ProfileViewModel(repo, session)


        viewModel.changePassword(
            current = "OldPass",
            new = "NewPass123",
            confirm = "OtraCosa"
        )


        val valState = viewModel.passwordValidationState.value


        assertEquals("Las nuevas contraseñas no coinciden", valState.confirmPasswordError)

        coVerify(exactly = 0) { repo.updatePassword(any(), any()) }

        println(" Prueba Validación Clave: Bloqueo por no coincidencia.")
    }

    @Test
    fun `cambiar contraseña exitosa llama al repositorio`() = runTest {

        val email = "user@test.com"
        val oldPass = "OldPass123"
        val newPass = "NewPass123"


        every { session.emailFlow } returns flowOf(email)


        val fakeUser = User("User", email, "", oldPass, UserRole.CLIENTE)
        coEvery { repo.getUserByEmail(email) } returns fakeUser


        coEvery { repo.login(email, oldPass) } returns fakeUser


        coEvery { repo.updatePassword(email, newPass) } just Runs

        viewModel = ProfileViewModel(repo, session)


        viewModel.changePassword(oldPass, newPass, newPass)


        val valState = viewModel.passwordValidationState.value


        assertTrue("Se esperaba éxito, pero falló con error: ${valState.currentPasswordError ?: valState.generalError}", valState.success)


        coVerify { repo.updatePassword(email, newPass) }

        println(" Prueba Cambio Clave: Flujo exitoso verificado.")
    }

    @Test
    fun `si falla la carga del perfil por red, muestra error`() = runTest {

        every { session.emailFlow } returns flowOf("user@test.com")

        coEvery { repo.getUserByEmail(any()) } throws IOException("Sin conexión")


        viewModel = ProfileViewModel(repo, session)


        val state = viewModel.uiState.value
        assertFalse(state.isLoading)

        assert(state.error!!.contains("Error de red"))

        println(" Prueba Error Perfil: Manejo de excepción correcto.")
    }
}