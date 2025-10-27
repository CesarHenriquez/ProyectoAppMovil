package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.viewmodel.ChatViewModel
import com.example.appmovilfitquality.data.local.MessageEntity
import com.example.appmovilfitquality.ui.components.ChatBubble
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import com.example.appmovilfitquality.util.AudioRecorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSupportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel
) {
    val ui by viewModel.ui.collectAsState()
    val context = LocalContext.current
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var recorder by remember { mutableStateOf<AudioRecorder?>(null) }
    var recording by remember { mutableStateOf(false) }

    //  Fija automáticamente el peer del soporte (primer usuario con rol STOCK)
    LaunchedEffect(Unit) {
        viewModel.ensureSupportPeerForClient()
    }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Soporte técnico", color = Color.White) },
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
                    // Campo de texto
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje…") },
                        singleLine = true
                    )

                    // Botón enviar texto
                    FilledIconButton(
                        onClick = {
                            val text = input.text.trim()
                            if (text.isNotEmpty()) {
                                viewModel.setInput(text)
                                viewModel.sendText()
                                input = TextFieldValue("")
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = GreenEnergy,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Enviar")
                    }

                    // Botón grabar audio
                    FilledIconButton(
                        onClick = {
                            if (!recording) {
                                if (recorder == null) recorder = AudioRecorder(context)
                                recorder?.start()
                                recording = true
                            } else {
                                val uri = recorder?.stop()
                                if (uri != null) {
                                    viewModel.sendAudio(uri.toString())
                                }
                                recording = false
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (recording) Color.Red else GreenEnergy,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Grabar audio")
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
                    reverseLayout = false
                ) {
                    items(ui.messages, key = { it.id }) { msg: MessageEntity ->
                        ChatBubble (
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