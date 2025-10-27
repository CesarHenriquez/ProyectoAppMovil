package com.example.appmovilfitquality.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        MessageEntity::class
    ],
    version = 6,                 // ⬅️ subimos a 6
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile private var INSTANCE: AppDataBase? = null


        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `messages` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `senderEmail` TEXT NOT NULL,
                        `receiverEmail` TEXT NOT NULL,
                        `text` TEXT,
                        `audioUri` TEXT,
                        `imageUri` TEXT,            
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }


        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {

                try { db.execSQL("ALTER TABLE products ADD COLUMN imageUri TEXT") } catch (_: Exception) {}


                try { db.execSQL("ALTER TABLE messages ADD COLUMN imageUri TEXT") } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "shared_messages_db"
                )

                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}