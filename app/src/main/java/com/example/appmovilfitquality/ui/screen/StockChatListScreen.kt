package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockChatListScreen(
    onNavigateBack: () -> Unit,
    onOpenChat: (peerEmail: String) -> Unit,
    viewModel: ChatViewModel
) {
    val counterparts by viewModel.counterparts.collectAsState()
    val me by viewModel.ui.collectAsState()

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Soporte – Conversaciones", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //  confirma con qué email está logeado STOCK
                Text(
                    text = "Sesión: ${me.me.ifBlank { "(sin email)" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )

                if (counterparts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aún no hay conversaciones.\nCuando un cliente te escriba, aparecerá aquí.",
                            color = Color.White
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(counterparts) { email ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenChat(email) }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Toca para abrir el chat",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}