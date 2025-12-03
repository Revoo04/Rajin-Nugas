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

    // --- BAGIAN USER ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registerUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginUser(email: String, password: String): User?

    // --- BAGIAN TASK (UPDATE PENTING DISINI) ---

    @Insert
    suspend fun insertTask(task: Task)

    // PERUBAHAN: Kita tambah WHERE user_id = :userId
    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY due_date ASC")
    fun getUserTasks(userId: Int): Flow<List<Task>>

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // Fitur Reset Password
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    suspend fun resetPassword(email: String, newPassword: String): Int
}