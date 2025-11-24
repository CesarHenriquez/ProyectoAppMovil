package com.example.appmovilfitquality.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.ChatRepository
import com.example.appmovilfitquality.data.repository.ChatRepository.MessageEntity // ⬅️ Usamos MessageEntity del repositorio simulado
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Constante para el chat grupal
private const val INTERNAL_GROUP_CHAT_ID = "internal_coordination_group"

// Estado de la UI del chat
data class ChatUi(
    val me: String = "",
    val peer: String = "",
    val messages: List<MessageEntity> = emptyList(),
    val inputText: String = "",
    val recording: Boolean = false
)


/**
 * ViewModel del Chat, ahora usa ChatRepository simulado en memoria.
 * NO USA Room.
 */
class ChatViewModel(
    private val repo: ChatRepository, // ⬅️ Inyección del repositorio simulado
    private val session: SessionManager
) : ViewModel() {

    // NOTA: Se eliminan las referencias a db, dao, y userDao

    private val _ui = MutableStateFlow(ChatUi())
    val ui: StateFlow<ChatUi> = _ui.asStateFlow()

    private val _counterparts = MutableStateFlow<List<String>>(emptyList())
    val counterparts: StateFlow<List<String>> = _counterparts.asStateFlow()

    private var messagesJob: kotlinx.coroutines.Job? = null
    private var player: MediaPlayer? = null

    init {
        // Cargar email del usuario actual desde DataStore
        viewModelScope.launch {
            session.emailFlow.collect { email ->
                if (!email.isNullOrBlank()) {
                    _ui.update { it.copy(me = email) }
                    // observeCounterparts(email) // Opcional en el flujo REST
                }
            }
        }
    }


    // ⬅️ Lógica para el Chat Grupal de Coordinación (STOCK/DELIVERY)
    fun openGroupChat() {
        _ui.update { it.copy(
            peer = INTERNAL_GROUP_CHAT_ID,
            messages = emptyList(),
            inputText = ""
        ) }
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            // El repositorio simulado usa el ID del grupo como si fuera un "peer"
            repo.conversation(_ui.value.me, INTERNAL_GROUP_CHAT_ID).collect { msgs ->
                _ui.update { it.copy(messages = msgs) }
            }
        }
    }

    /**
     * Fija automáticamente el peer del soporte (Chat Privado Cliente ↔ Admin).
     */
    fun ensureSupportPeerForClient() {
        // Asumimos el email del admin conocido para iniciar el chat de soporte
        val stockEmail = "admin@stock.com"
        openConversation(stockEmail)
    }


    // Abre conversación con un peer (Chat Privado)
    fun openConversation(peer: String) {
        _ui.update { it.copy(peer = peer, messages = emptyList(), inputText = "") }
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repo.conversation(_ui.value.me, peer).collect { msgs ->
                _ui.update { it.copy(messages = msgs) }
            }
        }
    }

    // Manejo del input
    fun setInput(text: String) {
        _ui.update { it.copy(inputText = text) }
    }

    // Función auxiliar para enviar mensajes
    private suspend fun sendMessage(message: MessageEntity) {
        repo.send(message)
    }


    // Envío de texto
    fun sendText() {
        val me = _ui.value.me
        val target = _ui.value.peer
        val body = _ui.value.inputText.trim()
        if (me.isBlank() || target.isBlank() || body.isBlank()) return

        viewModelScope.launch {
            sendMessage(
                MessageEntity(
                    senderEmail = me,
                    receiverEmail = target,
                    text = body,
                    audioUri = null,
                    imageUri = null,
                    timestamp = System.currentTimeMillis()
                )
            )
            _ui.update { it.copy(inputText = "") }
        }
    }


    // Envío de audio
    fun sendAudio(uriString: String) {
        val me = _ui.value.me
        val target = _ui.value.peer
        if (me.isBlank() || target.isBlank()) return

        viewModelScope.launch {
            sendMessage(
                MessageEntity(
                    senderEmail = me,
                    receiverEmail = target,
                    text = null,
                    audioUri = uriString,
                    imageUri = null,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Envío genérico (usado para enviar imágenes desde CameraCaptureRow)
    fun send(message: MessageEntity) {
        viewModelScope.launch {
            sendMessage(message)
        }
    }


    // Reproducir audio (mantenido)
    fun playAudio(context: Context, uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            player?.run {
                stop()
                reset()
                release()
            }
            player = null

            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(context.applicationContext, uri)
                setOnCompletionListener {
                    it.reset()
                    it.release()
                    player = null
                }
                setOnErrorListener { p, _, _ ->
                    try {
                        p.reset()
                        p.release()
                    } catch (_: Exception) { }
                    player = null
                    true
                }
                prepare()
                start()
            }
            player = mp
        } catch (_: Exception) { }
    }

    fun stopAudio() {
        try {
            player?.stop()
            player?.reset()
            player?.release()
        } catch (_: Exception) { }
        player = null
    }

    override fun onCleared() {
        stopAudio()
        messagesJob?.cancel()
        super.onCleared()
    }
}