package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.local.MessageDao
import com.example.appmovilfitquality.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dao: MessageDao) {
    fun conversation(a: String, b: String): Flow<List<MessageEntity>> = dao.conversation(a, b)

    suspend fun sendText(from: String, to: String, text: String) {
        val msg = MessageEntity(
            senderEmail = from,
            receiverEmail = to,
            text = text,
            audioUri = null,
            timestamp = System.currentTimeMillis()
        )
        dao.insert(msg)
    }

    suspend fun sendAudio(from: String, to: String, audioUri: String) {
        val msg = MessageEntity(
            senderEmail = from,
            receiverEmail = to,
            text = null,
            audioUri = audioUri,

            timestamp = System.currentTimeMillis()
        )
        dao.insert(msg)
    }

    suspend fun send(message: MessageEntity): Long =
        dao.insert(message)

    fun counterparts(me: String): Flow<List<String>> =
        dao.counterparts(me)
}