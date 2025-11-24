package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.data.repository.ChatRepository.MessageEntity // ⬅️ CORREGIDO: Importar modelo intermedio
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.ui.components.ChatBubble
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import com.example.appmovilfitquality.util.AudioRecorder
import com.example.appmovilfitquality.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockChatScreen(
    onNavigateBack: () -> Unit,
    peerEmail: String,
    viewModel: ChatViewModel
) {
    // Abrir/observar conversación con el cliente indicado
    LaunchedEffect(peerEmail) {
        if (peerEmail.isNotBlank()) viewModel.openConversation(peerEmail)
    }

    val ui by viewModel.ui.collectAsState()

    val context = LocalContext.current
    val recorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chat con $peerEmail", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = GreenEnergy)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .padding(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mic: grabar / detener y enviar
                    FilledIconButton(
                        onClick = {
                            if (!isRecording) {
                                recorder.start()
                                isRecording = true
                            } else {
                                val uri = recorder.stop()
                                isRecording = false
                                if (uri != null) viewModel.sendAudio(uri.toString())
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.15f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (isRecording) "Detener" else "Grabar"
                        )
                    }

                    OutlinedTextField(
                        value = ui.inputText,
                        onValueChange = { viewModel.setInput(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje…") },
                        singleLine = true
                    )

                    FilledIconButton(
                        onClick = {
                            scope.launch { viewModel.sendText() }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = GreenEnergy,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(ui.messages, key = { it.id }) { msg: MessageEntity -> // ⬅️ CORREGIDO
                        ChatBubble(
                            isMine = msg.senderEmail == ui.me,
                            entity = msg,
                            onPlayAudio = {
                                val uri = msg.audioUri
                                if (!uri.isNullOrBlank()) {

                                    viewModel.playAudio(context, uri)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}