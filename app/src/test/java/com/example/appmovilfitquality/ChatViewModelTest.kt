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

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0


        mockkStatic(android.net.Uri::class)
        every { Uri.parse(any()) } returns mockk()

        repo = mockk()
        session = mockk()
    }

    @Test
    fun `al iniciar, obtiene el email del usuario actual`() = runTest {

        val myEmail = "yo@test.com"
        every { session.emailFlow } returns flowOf(myEmail)


        viewModel = ChatViewModel(repo, session)


        assertEquals(myEmail, viewModel.ui.value.me)
        println(" Prueba Inicio Chat: Email de usuario cargado.")
    }

    @Test
    fun `abrir conversacion carga los mensajes del repositorio`() = runTest {

        val myEmail = "yo@test.com"
        val peerEmail = "otro@test.com"
        every { session.emailFlow } returns flowOf(myEmail)


        val fakeMessages = listOf(
            MessageEntity(1, myEmail, peerEmail, "Hola", null, null, 1000L)
        )
        coEvery { repo.conversation(myEmail, peerEmail) } returns flowOf(fakeMessages)

        viewModel = ChatViewModel(repo, session)


        viewModel.openConversation(peerEmail)


        val state = viewModel.ui.value


        assertEquals(peerEmail, state.peer)


        assertEquals(1, state.messages.size)
        assertEquals("Hola", state.messages[0].text)

        println(" Prueba Abrir Chat: Mensajes cargados.")
    }

    @Test
    fun `enviar texto llama al repositorio y limpia el input`() = runTest {

        val myEmail = "yo@test.com"
        val peerEmail = "otro@test.com"
        every { session.emailFlow } returns flowOf(myEmail)


        coEvery { repo.conversation(any(), any()) } returns flowOf(emptyList())


        coEvery { repo.send(any()) } returns 1L

        viewModel = ChatViewModel(repo, session)
        viewModel.openConversation(peerEmail)


        viewModel.setInput("Mensaje nuevo") // Escribimos
        viewModel.sendText() // Enviamos




        assertEquals("", viewModel.ui.value.inputText)


        coVerify {
            repo.send(match {
                it.text == "Mensaje nuevo" &&
                        it.senderEmail == myEmail &&
                        it.receiverEmail == peerEmail
            })
        }

        println(" Prueba Enviar Texto: Mensaje procesado correctamente.")
    }
}