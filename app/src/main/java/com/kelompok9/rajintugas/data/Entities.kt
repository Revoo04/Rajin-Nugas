package com.kelompok9.rajintugas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// --- TABEL USER (Untuk Login/Register) ---
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val user_id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: String // 'user' atau 'guest'
)

// --- TABEL PRIORITY (Warna & Level Prioritas) ---
@Entity(tableName = "priorities")
data class Priority(
    @PrimaryKey(autoGenerate = true) val priority_id: Int = 0,
    val level_name: String,
    val color_code: String
)

// --- TABEL TASK (Tugas Utama) ---
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["user_id"], childColumns = ["user_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Priority::class, parentColumns = ["priority_id"], childColumns = ["priority_id"])
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val task_id: Int = 0,
    val user_id: Int,
    val title: String,
    val description: String,
    val due_date: Long, // Format tanggal disimpan sebagai angka
    val status: String, // 'pending' atau 'done'
    val priority_id: Int,
    val created_at: Long = System.currentTimeMillis()
)

// --- TABEL REMINDER (Pengingat) ---
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(entity = Task::class, parentColumns = ["task_id"], childColumns = ["task_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val reminder_id: Int = 0,
    val task_id: Int,
    val reminder_time: Long,
    val is_sent: Boolean = false
)