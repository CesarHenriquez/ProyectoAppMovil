package com.example.appmovilfitquality.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appmovilfitquality.data.local.MessageEntity
import com.example.appmovilfitquality.ui.theme.GreenEnergy


@Composable
fun ChatBubble(
    isMine: Boolean,
    entity: MessageEntity,
    onPlayAudio: () -> Unit
) {
    val bubbleColor = if (isMine) GreenEnergy.copy(alpha = 0.15f) else Color(0xFF1C1C1E)
    val contentColor = if (isMine) Color.White else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        ElevatedCard(
            modifier = Modifier
                .widthIn(min = 80.dp, max = 320.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .background(bubbleColor)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Texto (si hay)
                entity.text?.let { txt ->
                    Text(
                        text = txt,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 20,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Imagen (si hay)
                entity.imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Imagen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .heightIn(min = 140.dp)
                    )
                }

                // Audio (si hay)
                entity.audioUri?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Mensaje de voz",
                            color = contentColor,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                        IconButton(onClick = onPlayAudio) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Reproducir", tint = contentColor)
                        }
                    }
                }
            }
        }
    }
}