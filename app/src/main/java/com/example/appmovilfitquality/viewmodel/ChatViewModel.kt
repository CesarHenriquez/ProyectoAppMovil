package com.example.appmovilfitquality.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.local.AppDataBase
import com.example.appmovilfitquality.data.local.MessageEntity
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


// Estado de la UI del chat

data class ChatUi(
    val me: String = "",
    val peer: String = "",
    val messages: List<MessageEntity> = emptyList(),
    val inputText: String = "",
    val recording: Boolean = false
)


// ViewModel del Chat

class ChatViewModel(
    context: Context,
    private val session: SessionManager
) : ViewModel() {

    private val db = AppDataBase.getDatabase(context)
    private val dao = db.messageDao()
    private val userDao = db.userDao() // para obtener el email del usuario STOCK
    private val repo = ChatRepository(dao)

    private val _ui = MutableStateFlow(ChatUi())
    val ui: StateFlow<ChatUi> = _ui.asStateFlow()

    private val _counterparts = MutableStateFlow<List<String>>(emptyList())
    val counterparts: StateFlow<List<String>> = _counterparts.asStateFlow()

    private var messagesJob: kotlinx.coroutines.Job? = null
    private var player: MediaPlayer? = null

    init {
        // Cargar email del usuario actual
        viewModelScope.launch {
            session.emailFlow.collect { email ->
                if (!email.isNullOrBlank()) {
                    _ui.update { it.copy(me = email) }
                    observeCounterparts(email)
                }
            }
        }
    }


    // Observa todas las contrapartes con las que he hablado

    private fun observeCounterparts(me: String) {
        viewModelScope.launch {
            repo.counterparts(me).collect { list ->
                _counterparts.value = list
            }
        }
    }


    // Fija automáticamente el peer (el primer STOCK registrado)

    fun ensureSupportPeerForClient() {
        viewModelScope.launch {
            if (_ui.value.peer.isBlank()) {
                val stockEmail = userDao.firstEmailByRole("STOCK")
                if (!stockEmail.isNullOrBlank()) {
                    _ui.update { it.copy(peer = stockEmail) }
                }
            }
        }
    }


    // Abre conversación con un peer

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


    // Envío de texto

    fun sendText() {
        val me = _ui.value.me
        val peer = _ui.value.peer
        val body = _ui.value.inputText.trim()
        if (me.isBlank() || peer.isBlank() || body.isBlank()) return

        viewModelScope.launch {
            repo.send(
                MessageEntity(
                    senderEmail = me,
                    receiverEmail = peer,
                    text = body,
                    audioUri = null,
                    timestamp = System.currentTimeMillis()
                )
            )
            _ui.update { it.copy(inputText = "") }
        }
    }


    // Envío de audio

    fun sendAudio(uriString: String) {
        val me = _ui.value.me
        val peer = _ui.value.peer
        if (me.isBlank() || peer.isBlank()) return

        viewModelScope.launch {
            repo.send(
                MessageEntity(
                    senderEmail = me,
                    receiverEmail = peer,
                    text = null,
                    audioUri = uriString,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }


    // Reproducir audio

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

