package com.kelompok9.rajintugas

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.kelompok9.rajintugas.data.AppDatabase
import com.kelompok9.rajintugas.data.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var rvTasks: RecyclerView
    private lateinit var tvWelcome: TextView
    private lateinit var tvCurrentDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. Inisialisasi Database
        db = AppDatabase.getDatabase(this)

        // 2. Kenalan sama komponen di Layout
        rvTasks = findViewById(R.id.rvTasks)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)

        // 3. Tampilkan Tanggal Hari Ini
        val today = SimpleDateFormat("EEEE, dd-MM-yyyy", Locale("id", "ID")).format(Date())
        tvCurrentDate.text = today

        // 4. Setup RecyclerView (List Tugas)
        setupRecyclerView()

        // 5. Ambil Data Tugas dari Database
        loadTasks()

        // 6. Setup MENU BAWAH (Bottom Navigation)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Matikan animasi geser biar rapi
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> {
                    // --- TOMBOL TAMBAH (TENGAH) ---
                    // PINDAH KE HALAMAN TAMBAH TUGAS (ADD TASK)
                    startActivity(Intent(this@DashboardActivity, AddTaskActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    // Sudah di Home
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this@DashboardActivity, "Menu Profil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> true
            }
        }

        // 7. Tampilkan Nama User
        val username = intent.getStringExtra("USERNAME") ?: "Sayang"
        tvWelcome.text = "Halo, $username!"
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onDeleteClick = { task ->
                deleteTask(task)
            },
            onStatusChange = { task, isChecked ->
                updateTaskStatus(task, isChecked)
            }
        )
        rvTasks.adapter = taskAdapter
        rvTasks.layoutManager = LinearLayoutManager(this)
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            db.appDao().getAllTasks().collect { tasks ->
                taskAdapter.setData(tasks)
            }
        }
    }

    private fun updateTaskStatus(task: Task, isDone: Boolean) {
        val newStatus = if (isDone) "done" else "pending"
        val updatedTask = task.copy(status = newStatus)
        lifecycleScope.launch {
            db.appDao().updateTask(updatedTask)
        }
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            db.appDao().deleteTask(task)
            Toast.makeText(this@DashboardActivity, "Tugas dihapus", Toast.LENGTH_SHORT).show()
        }
    }
}