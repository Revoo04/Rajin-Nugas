package com.kelompok9.rajintugas

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    private var currentUsername: String = "Sayang"
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = AppDatabase.getDatabase(this)

        rvTasks = findViewById(R.id.rvTasks)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)

        // Ambil Data User dari Saku
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        currentUsername = sharedPref.getString("USERNAME", "Sayang") ?: "Sayang"
        currentUserId = sharedPref.getInt("USER_ID", -1)

        tvWelcome.text = "Halo, $currentUsername!"

        val displayFormatter = SimpleDateFormat("EEEE, dd-MM-yyyy", Locale("id", "ID"))
        tvCurrentDate.text = displayFormatter.format(Date())

        setupRecyclerView()
        loadTasks()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> {
                    startActivity(Intent(this@DashboardActivity, AddTaskActivity::class.java))
                    true
                }
                R.id.nav_home -> true
                R.id.nav_tasks -> {
                    startActivity(Intent(this@DashboardActivity, TaskListActivity::class.java))
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this@DashboardActivity, CalendarActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this@DashboardActivity, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onDeleteClick = { task ->
                // Saat ditekan tahan -> Muncul Pilihan Edit/Hapus
                showEditDeleteDialog(task)
            },
            onStatusChange = { task, isChecked -> updateTaskStatus(task, isChecked) }
        )
        rvTasks.adapter = taskAdapter
        rvTasks.layoutManager = LinearLayoutManager(this)
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            db.appDao().getUserTasks(currentUserId).collect { allTasks ->
                // Filter: Hanya tampilkan tugas yang BELUM SELESAI (Pending)
                val pendingTasks = allTasks.filter { task ->
                    task.status == "pending"
                }

                // Sortir: Deadline terdekat di atas, lalu prioritas
                val sortedTasks = pendingTasks.sortedWith(compareBy(
                    { it.due_date },
                    { it.priority_id }
                ))

                taskAdapter.setData(sortedTasks)
            }
        }
    }

    // --- DIALOG PILIHAN EDIT/HAPUS ---
    private fun showEditDeleteDialog(task: Task) {
        val options = arrayOf("Edit Tugas", "Hapus Tugas")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Aksi: ${task.title}")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> { // Edit
                    val intent = Intent(this, AddTaskActivity::class.java)
                    intent.putExtra("TASK_ID", task.task_id)
                    intent.putExtra("TASK_TITLE", task.title)
                    intent.putExtra("TASK_DATE", task.due_date)
                    intent.putExtra("TASK_PRIORITY", task.priority_id)
                    startActivity(intent)
                }
                1 -> { // Hapus
                    confirmDeleteTask(task)
                }
            }
        }
        builder.show()
    }

    private fun updateTaskStatus(task: Task, isDone: Boolean) {
        if (isDone) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi Selesai")
            builder.setMessage("Yakin tugas '${task.title}' sudah selesai?")
            builder.setPositiveButton("Ya, Selesai!") { _, _ ->
                val updatedTask = task.copy(status = "done")
                lifecycleScope.launch {
                    db.appDao().updateTask(updatedTask)
                    Toast.makeText(this@DashboardActivity, "Mantap! Tugas selesai.", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Belum") { dialog, _ ->
                taskAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }
            builder.show()
        } else {
            val updatedTask = task.copy(status = "pending")
            lifecycleScope.launch {
                db.appDao().updateTask(updatedTask)
            }
        }
    }

    private fun confirmDeleteTask(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hapus Tugas")
        builder.setMessage("Yakin mau menghapus tugas '${task.title}'?")
        builder.setPositiveButton("Hapus") { _, _ ->
            lifecycleScope.launch {
                db.appDao().deleteTask(task)
                Toast.makeText(this@DashboardActivity, "Tugas dihapus", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}