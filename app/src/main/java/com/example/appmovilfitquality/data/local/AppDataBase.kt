package com.example.appmovilfitquality.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        MessageEntity::class,
        OrderItemEntity::class // Entidad de Trazabilidad
    ],
    version = 8,                 // Actualizado a Versión 8 (Stock)
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun messageDao(): MessageDao
    abstract fun orderItemDao(): OrderItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        // MIGRACIÓN V4 A V5 (Trazabilidad - Inicial)
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

        // MIGRACIÓN V5 A V6
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE products ADD COLUMN imageUri TEXT") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE messages ADD COLUMN imageUri TEXT") } catch (_: Exception) {}
            }
        }

        // MIGRACIÓN V6 A V7 (Trazabilidad - Añade OrderItemEntity y timestamp)
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear la nueva tabla de OrderItemEntity
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `order_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `orderId` INTEGER NOT NULL,
                        `productId` INTEGER NOT NULL,
                        `productName` TEXT NOT NULL,
                        `productPrice` REAL NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                // Añadir la columna 'timestamp' a la tabla de órdenes
                val defaultTimestamp = System.currentTimeMillis()
                db.execSQL("ALTER TABLE orders ADD COLUMN timestamp INTEGER NOT NULL DEFAULT $defaultTimestamp")
            }
        }

        // MIGRACIÓN V7 A V8: Añadir la columna 'stock' a ProductEntity
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Añadir la columna 'stock' a la tabla de productos con valor por defecto 0
                db.execSQL("ALTER TABLE products ADD COLUMN stock INTEGER NOT NULL DEFAULT 0")
            }
        }


        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "shared_messages_db"
                )
                    // Incluye todas las migraciones
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}