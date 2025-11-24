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
        // 1. Mockear Log de Android
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        repo = mockk()
        session = mockk(relaxed = true)

        // 2. CRÍTICO: El ViewModel lee el rol en el init{}, debemos simularlo.
        every { session.roleFlow } returns flowOf(UserRole.GUEST)

        // Instanciamos el ViewModel
        viewModel = AuthViewModel(repo, session)
    }

    @Test
    fun `login exitoso actualiza el estado del rol`() = runTest {
        // --- 1. PREPARACIÓN ---
        val email = "admin@test.com"
        val pass = "12345"
        val mockUser = mockk<AuthRepository.User>()

        // Simulamos que el usuario devuelto tiene rol STOCK
        every { mockUser.role } returns UserRole.STOCK

        // Simulamos que el repo devuelve el usuario correctamente
        coEvery { repo.login(email, pass) } returns mockUser

        // --- 2. EJECUCIÓN ---
        // Usamos una lista mutable para capturar el resultado del callback
        var resultRecibido: LoginResult? = null

        viewModel.login(email, pass) { result ->
            resultRecibido = result
        }

        // --- 3. VERIFICACIÓN ---
        // A) El callback debe haber recibido Success
        assertTrue(resultRecibido is LoginResult.Success)

        // B) El rol dentro del Success debe ser STOCK
        assertEquals(UserRole.STOCK, (resultRecibido as LoginResult.Success).role)

        // C) El estado observable del ViewModel debe haberse actualizado
        assertEquals(UserRole.STOCK, viewModel.currentUserRole.value)

        println("✅ Prueba Login Exitoso: Rol actualizado correctamente.")
    }

    @Test
    fun `login fallido por credenciales incorrectas devuelve WrongPassword`() = runTest {
        // --- 1. PREPARACIÓN ---
        // Simulamos que el repo lanza una excepción (error 401 o similar)
        coEvery { repo.login(any(), any()) } throws Exception("Bad credentials")

        // --- 2. EJECUCIÓN ---
        var resultRecibido: LoginResult? = null
        viewModel.login("user", "bad_pass") {
            resultRecibido = it
        }

        // --- 3. VERIFICACIÓN ---
        // Debe ser WrongPassword (que es lo que tu VM devuelve por defecto ante errores)
        assertTrue(resultRecibido is LoginResult.WrongPassword)

        // El rol no debe haber cambiado (sigue siendo GUEST del inicio)
        assertEquals(UserRole.GUEST, viewModel.currentUserRole.value)

        println("✅ Prueba Login Fallido: Error manejado correctamente.")
    }

    @Test
    fun `registro falla si las contraseñas no coinciden y NO llama al repositorio`() = runTest {
        // --- 1. EJECUCIÓN ---
        // Intentamos registrar con claves distintas "123" vs "abc"
        viewModel.validateAndRegister(
            name = "Pepe",
            email = "pepe@test.com",
            phone = "12345678",
            password = "123",
            confirm = "abc"
        )

        // --- 2. VERIFICACIÓN ---
        val validationState = viewModel.registerValidationState.value

        // A) Debe haber error de confirmación
        assertEquals("Las contraseñas no coinciden", validationState.confirmError)

        // B) No debe haber éxito
        assertFalse(viewModel.registrationSuccess.value)

        // C) CRÍTICO: Aseguramos que NUNCA se llamó a la API (ahorramos recursos)
        coVerify(exactly = 0) { repo.registerUser(any()) }

        println("✅ Prueba Validación Registro: Bloqueado por claves distintas.")
    }

    @Test
    fun `registro exitoso llama al repositorio y limpia errores`() = runTest {
        // --- 1. PREPARACIÓN ---
        // Simulamos respuesta exitosa del repo
        coEvery { repo.registerUser(any()) } returns mockk()

        // --- 2. EJECUCIÓN ---
        // Datos válidos (Pass segura: Mayúscula + Número)
        viewModel.validateAndRegister(
            name = "Pepe",
            email = "pepe@test.com",
            phone = "12345678",
            password = "Password123",
            confirm = "Password123"
        )

        // --- 3. VERIFICACIÓN ---
        // A) Debe indicar éxito
        assertTrue(viewModel.registrationSuccess.value)

        // B) No debe haber errores
        assertEquals(null, viewModel.registerValidationState.value.generalError)

        // C) Debe haber llamado al repo 1 vez
        coVerify(exactly = 1) { repo.registerUser(any()) }

        println("✅ Prueba Registro OK: Llamada a API correcta.")
    }
}