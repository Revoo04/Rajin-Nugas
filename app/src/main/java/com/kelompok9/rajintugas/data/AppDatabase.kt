package com.kelompok9.rajintugas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Di sini kita daftarkan semua tabel yang ada di Entities.kt
@Database(entities = [User::class, Task::class, Priority::class, Reminder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Daftarkan DAO yang sudah kita buat
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Fungsi ini buat bikin database-nya cuma sekali (Singleton) biar hemat memori
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rajin_tugas_database" // Nama file database di HP nanti
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}