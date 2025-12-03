package com.kelompok9.rajintugas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// --- TABEL USER ---
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val user_id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: String
)

// --- TABEL PRIORITY ---
@Entity(tableName = "priorities")
data class Priority(
    @PrimaryKey(autoGenerate = true) val priority_id: Int = 0,
    val level_name: String,
    val color_code: String
)

// --- TABEL TASK  ---
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val task_id: Int = 0,
    val user_id: Int,
    val title: String,
    val description: String,
    val due_date: Long,
    val status: String,
    val priority_id: Int,
    val created_at: Long = System.currentTimeMillis()
)

// --- TABEL REMINDER ---
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val reminder_id: Int = 0,
    val task_id: Int,
    val reminder_time: Long,
    val is_sent: Boolean = false
)