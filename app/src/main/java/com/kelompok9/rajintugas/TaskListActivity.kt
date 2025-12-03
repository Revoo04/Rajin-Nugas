package com.kelompok9.rajintugas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kelompok9.rajintugas.data.AppDatabase
import com.kelompok9.rajintugas.data.Task
import kotlinx.coroutines.launch

class TaskListActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var taskAdapter: TaskAdapter
    private var allTasks: List<Task> = listOf()
    private var currentUserId: Int = -1

    private lateinit var btnAll: Button
    private lateinit var btnPending: Button
    private lateinit var btnDone: Button
    private lateinit var etSearch: EditText
    private var currentFilterStatus: String = "all"
    private var currentSearchText: String = ""

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            taskAdapter.notifyDataSetChanged()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        db = AppDatabase.getDatabase(this)
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("USER_ID", -1)

        val rvTaskList = findViewById<RecyclerView>(R.id.rvTaskList)
        rvTaskList.itemAnimator = null

        // --- PERBAIKAN: Ganti 'onItemClick' jadi 'onDeleteClick' ---
        taskAdapter = TaskAdapter(
            onDeleteClick = { task ->
                showEditDeleteDialog(task) // Munculkan dialog pilihan
            },
            onStatusChange = { task, isChecked -> updateTaskStatus(task, isChecked) }
        )
        // ----------------------------------------------------------

        rvTaskList.adapter = taskAdapter
        rvTaskList.layoutManager = LinearLayoutManager(this)

        btnAll = findViewById(R.id.btnFilterAll)
        btnPending = findViewById(R.id.btnFilterPending)
        btnDone = findViewById(R.id.btnFilterDone)
        etSearch = findViewById(R.id.etSearch)

        btnAll.setOnClickListener { updateFilter("all") }
        btnPending.setOnClickListener { updateFilter("pending") }
        btnDone.setOnClickListener { updateFilter("done") }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s.toString()
                applyCombinedFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadTasks()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_tasks

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTaskActivity::class.java))
                    true
                }
                R.id.nav_tasks -> true
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimeRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            db.appDao().getUserTasks(currentUserId).collect { tasks ->
                allTasks = tasks
                applyCombinedFilter()
            }
        }
    }

    private fun updateFilter(status: String) {
        currentFilterStatus = status
        resetButtonColors()
        when (status) {
            "pending" -> setActiveButtonColor(btnPending)
            "done" -> setActiveButtonColor(btnDone)
            else -> setActiveButtonColor(btnAll)
        }
        applyCombinedFilter()
    }

    private fun applyCombinedFilter() {
        var filteredList = allTasks
        if (currentFilterStatus == "pending") {
            filteredList = filteredList.filter { it.status == "pending" }
        } else if (currentFilterStatus == "done") {
            filteredList = filteredList.filter { it.status == "done" }
        }
        if (currentSearchText.isNotEmpty()) {
            filteredList = filteredList.filter { task ->
                task.title.contains(currentSearchText, ignoreCase = true)
            }
        }
        taskAdapter.setData(filteredList)
    }

    private fun resetButtonColors() {
        listOf(btnAll, btnPending, btnDone).forEach { btn ->
            btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
            btn.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private fun setActiveButtonColor(activeBtn: Button) {
        activeBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_blue)
        activeBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun updateTaskStatus(task: Task, isDone: Boolean) {
        val builder = AlertDialog.Builder(this)
        if (isDone) {
            builder.setTitle("Konfirmasi Selesai")
            builder.setMessage("Yakin tugas '${task.title}' sudah selesai?")
            builder.setPositiveButton("Ya, Selesai!") { _, _ ->
                lifecycleScope.launch {
                    db.appDao().updateTask(task.copy(status = "done"))
                    Toast.makeText(this@TaskListActivity, "Kerja bagus!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            builder.setTitle("Batalkan Selesai")
            builder.setMessage("Kembalikan jadi BELUM selesai?")
            builder.setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    db.appDao().updateTask(task.copy(status = "pending"))
                }
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            taskAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }
        builder.show()
    }

    // --- FUNGSI DIALOG PILIHAN (EDIT / HAPUS) ---
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

    private fun confirmDeleteTask(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hapus Tugas")
        builder.setMessage("Yakin hapus '${task.title}'?")
        builder.setPositiveButton("Hapus") { _, _ ->
            lifecycleScope.launch {
                db.appDao().deleteTask(task)
                Toast.makeText(this@TaskListActivity, "Terhapus.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}