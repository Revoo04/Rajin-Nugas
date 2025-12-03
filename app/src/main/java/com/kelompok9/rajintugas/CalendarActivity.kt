package com.kelompok9.rajintugas

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kelompok9.rajintugas.data.AppDatabase
import com.kelompok9.rajintugas.data.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var tvBusyDates: TextView
    private var allTasks: List<Task> = listOf()
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        db = AppDatabase.getDatabase(this)
        tvBusyDates = findViewById(R.id.tvBusyDates)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)

        val rvCalendarTasks = findViewById<RecyclerView>(R.id.rvCalendarTasks)

        // --- Setup Adapter dengan Fitur Edit/Hapus ---
        taskAdapter = TaskAdapter(
            onDeleteClick = { task -> showEditDeleteDialog(task) },
            onStatusChange = { task, isChecked -> updateStatus(task, isChecked) }
        )

        rvCalendarTasks.adapter = taskAdapter
        rvCalendarTasks.layoutManager = LinearLayoutManager(this)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        loadAllTasks {
            showBusyDatesInfo()
            val today = Calendar.getInstance().timeInMillis
            filterTasksByDate(today)
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            filterTasksByDate(selectedDate.timeInMillis)
        }

        setupBottomNav()
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

    private fun loadAllTasks(onLoaded: () -> Unit) {
        lifecycleScope.launch {
            db.appDao().getUserTasks(currentUserId).collect { tasks ->
                allTasks = tasks
                onLoaded()
            }
        }
    }

    private fun showBusyDatesInfo() {
        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
        val pendingTasks = allTasks.filter { it.status == "pending" }

        if (pendingTasks.isEmpty()) {
            tvBusyDates.text = "📅 Tidak ada deadline mendatang. Aman!"
        } else {
            val busyDates = pendingTasks.map { task ->
                formatter.format(task.due_date)
            }.distinct().sorted()
            val datesString = busyDates.joinToString(", ")
            tvBusyDates.text = "📅 Tanggal ada tugas: $datesString"
        }
    }

    private fun filterTasksByDate(timeInMillis: Long) {
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val selectedStr = formatter.format(timeInMillis)

        val filtered = allTasks.filter {
            val taskDateStr = formatter.format(it.due_date)
            taskDateStr == selectedStr
        }
        taskAdapter.setData(filtered)
    }

    private fun updateStatus(task: Task, isDone: Boolean) {
        val builder = AlertDialog.Builder(this)
        if (isDone) {
            builder.setTitle("Konfirmasi Selesai")
            builder.setMessage("Yakin tugas '${task.title}' di tanggal ini sudah selesai?")
            builder.setPositiveButton("Ya, Selesai!") { _, _ ->
                lifecycleScope.launch {
                    db.appDao().updateTask(task.copy(status = "done"))
                    showBusyDatesInfo()
                    Toast.makeText(this@CalendarActivity, "Mantap! Tugas selesai.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            builder.setTitle("Batalkan Selesai")
            builder.setMessage("Kembalikan status jadi BELUM selesai?")
            builder.setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    db.appDao().updateTask(task.copy(status = "pending"))
                    showBusyDatesInfo()
                }
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            taskAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }
        builder.show()
    }

    private fun confirmDeleteTask(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hapus Tugas")
        builder.setMessage("Yakin mau menghapus '${task.title}'?")
        builder.setPositiveButton("Hapus") { _, _ ->
            lifecycleScope.launch {
                db.appDao().deleteTask(task)
                showBusyDatesInfo()
                Toast.makeText(this@CalendarActivity, "Tugas dihapus.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_calendar

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tasks -> {
                    startActivity(Intent(this, TaskListActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTaskActivity::class.java))
                    true
                }
                R.id.nav_calendar -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}