package com.example.appmovilfitquality

import android.net.Uri
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.ChatRepository
import com.example.appmovilfitquality.data.repository.ChatRepository.MessageEntity
import com.example.appmovilfitquality.viewmodel.ChatViewModel
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals

import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: ChatRepository
    private lateinit var session: SessionManager
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        // 1. Mockear Logs y Uri (porque ChatViewModel usa Uri.parse)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        // Mockear Uri.parse para que no falle en tests locales
        mockkStatic(android.net.Uri::class)
        every { Uri.parse(any()) } returns mockk()

        repo = mockk()
        session = mockk()
    }

    @Test
    fun `al iniciar, obtiene el email del usuario actual`() = runTest {
        // --- 1. PREPARACIÓN ---
        val myEmail = "yo@test.com"
        every { session.emailFlow } returns flowOf(myEmail)

        // --- 2. EJECUCIÓN ---
        viewModel = ChatViewModel(repo, session)

        // --- 3. VERIFICACIÓN ---
        assertEquals(myEmail, viewModel.ui.value.me)
        println("✅ Prueba Inicio Chat: Email de usuario cargado.")
    }

    @Test
    fun `abrir conversacion carga los mensajes del repositorio`() = runTest {
        // --- 1. PREPARACIÓN ---
        val myEmail = "yo@test.com"
        val peerEmail = "otro@test.com"
        every { session.emailFlow } returns flowOf(myEmail)

        // Simulamos mensajes existentes
        val fakeMessages = listOf(
            MessageEntity(1, myEmail, peerEmail, "Hola", null, null, 1000L)
        )
        coEvery { repo.conversation(myEmail, peerEmail) } returns flowOf(fakeMessages)

        viewModel = ChatViewModel(repo, session)

        // --- 2. EJECUCIÓN ---
        viewModel.openConversation(peerEmail)

        // --- 3. VERIFICACIÓN ---
        val state = viewModel.ui.value

        // A) El peer debe estar configurado
        assertEquals(peerEmail, state.peer)

        // B) La lista de mensajes debe tener 1 elemento
        assertEquals(1, state.messages.size)
        assertEquals("Hola", state.messages[0].text)

        println("✅ Prueba Abrir Chat: Mensajes cargados.")
    }

    @Test
    fun `enviar texto llama al repositorio y limpia el input`() = runTest {
        // --- 1. PREPARACIÓN ---
        val myEmail = "yo@test.com"
        val peerEmail = "otro@test.com"
        every { session.emailFlow } returns flowOf(myEmail)

        // Mock del flujo de conversación (vacío al principio)
        coEvery { repo.conversation(any(), any()) } returns flowOf(emptyList())

        // Mock de envío (retorna ID 1)
        coEvery { repo.send(any()) } returns 1L

        viewModel = ChatViewModel(repo, session)
        viewModel.openConversation(peerEmail)

        // --- 2. EJECUCIÓN ---
        viewModel.setInput("Mensaje nuevo") // Escribimos
        viewModel.sendText() // Enviamos

        // --- 3. VERIFICACIÓN ---

        // A) El input debe haberse limpiado
        assertEquals("", viewModel.ui.value.inputText)

        // B) Se debió llamar al repositorio para guardar
        coVerify {
            repo.send(match {
                it.text == "Mensaje nuevo" &&
                        it.senderEmail == myEmail &&
                        it.receiverEmail == peerEmail
            })
        }

        println("✅ Prueba Enviar Texto: Mensaje procesado correctamente.")
    }
}