package com.example.appmovilfitquality.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderEmail: String,
    val receiverEmail: String,
    val text: String?,
    val audioUri: String?,
    val imageUri: String? = null,
    val timestamp: Long,
)