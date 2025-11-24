package com.example.appmovilfitquality.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update


class ChatRepository {


    private data class MessageStoreDto(
        val id: Long = 0,
        val senderEmail: String,
        val receiverEmail: String,
        val text: String?,
        val audioUri: String?,
        val imageUri: String? = null,
        val timestamp: Long,
    )


    data class MessageEntity(
        val id: Long = 0,
        val senderEmail: String,
        val receiverEmail: String,
        val text: String?,
        val audioUri: String?,
        val imageUri: String? = null,
        val timestamp: Long,
    )

    private fun MessageStoreDto.toDomainEntity() = MessageEntity(
        id = this.id,
        senderEmail = this.senderEmail,
        receiverEmail = this.receiverEmail,
        text = this.text,
        audioUri = this.audioUri,
        imageUri = this.imageUri,
        timestamp = this.timestamp
    )


    private val messagesStore = MutableStateFlow<List<MessageStoreDto>>(emptyList())


    private var nextId = 1L


    fun conversation(a: String, b: String): Flow<List<MessageEntity>> = messagesStore.map { msgs ->
        msgs.filter {

            (it.senderEmail == a && it.receiverEmail == b) ||
                    (it.senderEmail == b && it.receiverEmail == a)
        }
            .map { it.toDomainEntity() }
            .sortedBy { it.timestamp }
    }


    suspend fun send(message: MessageEntity): Long {
        val msgDto = MessageStoreDto(
            id = nextId++,
            senderEmail = message.senderEmail,
            receiverEmail = message.receiverEmail,
            text = message.text,
            audioUri = message.audioUri,
            imageUri = message.imageUri,
            timestamp = System.currentTimeMillis()
        )

        messagesStore.update { it + msgDto }
        return msgDto.id
    }


    fun counterparts(me: String): Flow<List<String>> = messagesStore.map { msgs ->
        msgs.filter { it.senderEmail == me || it.receiverEmail == me }
            .map {

                if (it.senderEmail == me) it.receiverEmail else it.senderEmail
            }
            .distinct()
    }
}