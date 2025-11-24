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
        // 1. Mockear Logs para evitar errores de "Method not mocked"
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        repo = mockk()
        session = mockk()
    }

    @Test
    fun `al iniciar, carga el perfil del usuario desde el email de sesion`() = runTest {
        // --- 1. PREPARACIÓN ---
        val email = "user@test.com"
        val fakeUser = User(
            name = "Usuario Test",
            email = email,
            phone = "123456",
            password = "",
            role = UserRole.CLIENTE
        )

        // Simulamos que hay un email guardado en sesión
        every { session.emailFlow } returns flowOf(email)
        // Simulamos que el repo encuentra al usuario
        coEvery { repo.getUserByEmail(email) } returns fakeUser

        // --- 2. EJECUCIÓN ---
        // Al instanciar, el ViewModel llama a loadProfile() automáticamente
        viewModel = ProfileViewModel(repo, session)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value

        // A) No debe estar cargando
        assertFalse(state.isLoading)

        // B) Los datos deben coincidir
        assertEquals("Usuario Test", state.name)
        assertEquals("123456", state.phone)
        assertEquals("CLIENTE", state.role)

        println("✅ Prueba Carga Perfil: Datos obtenidos correctamente.")
    }

    @Test
    fun `cambiar contraseña falla si la confirmacion no coincide`() = runTest {
        // Inicializamos VM (simulamos sesión vacía para que no cargue nada en el init)
        every { session.emailFlow } returns flowOf("")
        viewModel = ProfileViewModel(repo, session)

        // --- EJECUCIÓN ---
        // Intentamos cambiar clave con confirmación errónea
        viewModel.changePassword(
            current = "OldPass",
            new = "NewPass123",
            confirm = "OtraCosa"
        )

        // --- VERIFICACIÓN ---
        val valState = viewModel.passwordValidationState.value

        // Debe haber error de confirmación
        assertEquals("Las nuevas contraseñas no coinciden", valState.confirmPasswordError)
        // No debió llamar a la API
        coVerify(exactly = 0) { repo.updatePassword(any(), any()) }

        println("✅ Prueba Validación Clave: Bloqueo por no coincidencia.")
    }

    @Test
    fun `cambiar contraseña exitosa llama al repositorio`() = runTest {
        // --- 1. PREPARACIÓN ---
        val email = "user@test.com"
        val oldPass = "OldPass123"
        val newPass = "NewPass123"

        // Configuramos el estado inicial del VM manualmente para tener el email
        every { session.emailFlow } returns flowOf(email)

        // Mock del usuario
        val fakeUser = User("User", email, "", oldPass, UserRole.CLIENTE)
        coEvery { repo.getUserByEmail(email) } returns fakeUser

        // ⬇️ CORRECCIÓN CRÍTICA: Mockear el Login exitoso (el paso de seguridad previo al update) ⬇️
        coEvery { repo.login(email, oldPass) } returns fakeUser

        // Mock de updatePassword (éxito)
        coEvery { repo.updatePassword(email, newPass) } just Runs

        viewModel = ProfileViewModel(repo, session) // Se carga el perfil (init)

        // --- 2. EJECUCIÓN ---
        viewModel.changePassword(oldPass, newPass, newPass)

        // --- 3. VERIFICACIÓN ---
        val valState = viewModel.passwordValidationState.value

        // A) Debe ser exitoso
        assertTrue("Se esperaba éxito, pero falló con error: ${valState.currentPasswordError ?: valState.generalError}", valState.success)

        // B) Se debió llamar al update en la API
        coVerify { repo.updatePassword(email, newPass) }

        println("✅ Prueba Cambio Clave: Flujo exitoso verificado.")
    }

    @Test
    fun `si falla la carga del perfil por red, muestra error`() = runTest {
        // --- 1. PREPARACIÓN ---
        every { session.emailFlow } returns flowOf("user@test.com")
        // Simulamos error de red
        coEvery { repo.getUserByEmail(any()) } throws IOException("Sin conexión")

        // --- 2. EJECUCIÓN ---
        viewModel = ProfileViewModel(repo, session)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        // Verificamos que el mensaje de error se asignó
        assert(state.error!!.contains("Error de red"))

        println("✅ Prueba Error Perfil: Manejo de excepción correcto.")
    }
}