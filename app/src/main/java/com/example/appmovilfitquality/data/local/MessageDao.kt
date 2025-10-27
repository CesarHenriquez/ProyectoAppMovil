package com.example.appmovilfitquality.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {


    @Query(
        """
        SELECT * FROM messages
        WHERE (senderEmail = :a AND receiverEmail = :b)
           OR (senderEmail = :b AND receiverEmail = :a)
        ORDER BY timestamp ASC
        """
    )
    fun conversation(a: String, b: String): Flow<List<MessageEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: MessageEntity): Long


    @Query("DELETE FROM messages")
    suspend fun clearAll()


    @Query(
        """
        SELECT peer FROM (
            SELECT 
                CASE 
                    WHEN senderEmail = :me THEN receiverEmail 
                    ELSE senderEmail 
                END AS peer,
                MAX(timestamp) AS lastTs
            FROM messages
            WHERE senderEmail = :me OR receiverEmail = :me
            GROUP BY peer
            ORDER BY lastTs DESC
        )
        """
    )
    fun counterparts(me: String): Flow<List<String>>
}