package com.kelompok9.rajintugas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- BAGIAN USER (Login & Register - RTG-001) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registerUser(user: User)

    // Mencari user berdasarkan email & password (untuk Login)
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginUser(email: String, password: String): User?


    // --- BAGIAN TASK (Manajemen Tugas - RTG-002) ---

    // Create: Tambah Tugas Baru
    @Insert
    suspend fun insertTask(task: Task)

    // Read: Ambil semua tugas, diurutkan dari deadline paling dekat
    @Query("SELECT * FROM tasks ORDER BY due_date ASC")
    fun getAllTasks(): Flow<List<Task>>

    // Update: Edit tugas atau checklist selesai
    @Update
    suspend fun updateTask(task: Task)

    // Delete: Hapus tugas
    @Delete
    suspend fun deleteTask(task: Task)


    // --- BAGIAN PRIORITAS (Untuk Dropdown Warna) ---

    @Query("SELECT * FROM priorities")
    suspend fun getPriorities(): List<Priority>

    @Insert
    suspend fun insertPriority(priority: Priority)

    // Fitur Tambahan: Update Password (Reset)
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    suspend fun resetPassword(email: String, newPassword: String): Int
}