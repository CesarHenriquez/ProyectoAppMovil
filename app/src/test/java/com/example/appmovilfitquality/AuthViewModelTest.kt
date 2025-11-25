package com.example.appmovilfitquality

import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.domain.model.UserRole
import com.example.appmovilfitquality.viewmodel.AuthViewModel
import com.example.appmovilfitquality.viewmodel.LoginResult
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: AuthRepository
    private lateinit var session: SessionManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        repo = mockk()
        session = mockk(relaxed = true)


        every { session.roleFlow } returns flowOf(UserRole.GUEST)


        viewModel = AuthViewModel(repo, session)
    }

    @Test
    fun `login exitoso actualiza el estado del rol`() = runTest {

        val email = "admin@test.com"
        val pass = "12345"
        val mockUser = mockk<AuthRepository.User>()


        every { mockUser.role } returns UserRole.STOCK


        coEvery { repo.login(email, pass) } returns mockUser


        var resultRecibido: LoginResult? = null

        viewModel.login(email, pass) { result ->
            resultRecibido = result
        }



        assertTrue(resultRecibido is LoginResult.Success)


        assertEquals(UserRole.STOCK, (resultRecibido as LoginResult.Success).role)


        assertEquals(UserRole.STOCK, viewModel.currentUserRole.value)

        println(" Prueba Login Exitoso: Rol actualizado correctamente.")
    }

    @Test
    fun `login fallido por credenciales incorrectas devuelve WrongPassword`() = runTest {

        coEvery { repo.login(any(), any()) } throws Exception("Bad credentials")


        var resultRecibido: LoginResult? = null
        viewModel.login("user", "bad_pass") {
            resultRecibido = it
        }


        assertTrue(resultRecibido is LoginResult.WrongPassword)


        assertEquals(UserRole.GUEST, viewModel.currentUserRole.value)

        println(" Prueba Login Fallido: Error manejado correctamente.")
    }

    @Test
    fun `registro falla si las contraseñas no coinciden y NO llama al repositorio`() = runTest {
        viewModel.validateAndRegister(
            name = "Pepe",
            email = "pepe@test.com",
            phone = "12345678",
            password = "123",
            confirm = "abc"
        )


        val validationState = viewModel.registerValidationState.value


        assertEquals("Las contraseñas no coinciden", validationState.confirmError)


        assertFalse(viewModel.registrationSuccess.value)


        coVerify(exactly = 0) { repo.registerUser(any()) }

        println(" Prueba Validación Registro: Bloqueado por claves distintas.")
    }

    @Test
    fun `registro exitoso llama al repositorio y limpia errores`() = runTest {

        coEvery { repo.registerUser(any()) } returns mockk()


        viewModel.validateAndRegister(
            name = "Pepe",
            email = "pepe@test.com",
            phone = "12345678",
            password = "Password123",
            confirm = "Password123"
        )


        assertTrue(viewModel.registrationSuccess.value)


        assertEquals(null, viewModel.registerValidationState.value.generalError)


        coVerify(exactly = 1) { repo.registerUser(any()) }

        println(" Prueba Registro OK: Llamada a API correcta.")
    }
}