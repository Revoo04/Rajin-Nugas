package com.kelompok9.rajintugas

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kelompok9.rajintugas.data.AppDatabase
import com.kelompok9.rajintugas.data.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class AddTaskActivity : AppCompatActivity() {

    private val calendar = Calendar.getInstance()
    private var isEditMode = false
    private var taskIdToEdit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val db = AppDatabase.getDatabase(this)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDate = findViewById<EditText>(R.id.etDate)
        val rgPriority = findViewById<RadioGroup>(R.id.rgPriority)
        val btnSave = findViewById<Button>(R.id.btnSaveTask)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Coba cari Header Title (Pastikan di XML sudah ada ID tvHeaderTitle)
        // Kalau error merah, abaikan dulu atau cek XML
        val tvHeader = findViewById<TextView>(R.id.tvHeaderTitle)

        // --- CEK MODE EDIT ---
        if (intent.hasExtra("TASK_ID")) {
            isEditMode = true
            taskIdToEdit = intent.getIntExtra("TASK_ID", 0)

            // Ganti Judul Halaman & Tombol
            if (tvHeader != null) tvHeader.text = "Edit Tugas"
            btnSave.text = "UPDATE TUGAS"

            // Isi data lama
            etTitle.setText(intent.getStringExtra("TASK_TITLE"))
            val oldDate = intent.getLongExtra("TASK_DATE", 0L)
            if (oldDate != 0L) {
                calendar.timeInMillis = oldDate
                val format = "dd-MM-yyyy HH:mm"
                etDate.setText(SimpleDateFormat(format, Locale.getDefault()).format(oldDate))
            }

            val oldPriority = intent.getIntExtra("TASK_PRIORITY", 3)
            when(oldPriority) {
                1 -> rgPriority.check(R.id.rbHigh)
                2 -> rgPriority.check(R.id.rbMedium)
                else -> rgPriority.check(R.id.rbLow)
            }
        }

        btnBack.setOnClickListener { finish() }
        etDate.setOnClickListener { showDateTimePicker(etDate) }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val dateStr = etDate.text.toString()

            if (title.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "Isi judul dan waktu deadline dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val priorityId = when (rgPriority.checkedRadioButtonId) {
                R.id.rbHigh -> 1
                R.id.rbMedium -> 2
                else -> 3
            }

            val dateLong = calendar.timeInMillis
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            val currentUserId = sharedPref.getInt("USER_ID", -1)

            if (currentUserId == -1) {
                Toast.makeText(this, "Sesi habis, login ulang.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kalau Edit -> Pakai ID lama. Kalau Baru -> Bikin ID baru.
            val finalTaskId = if (isEditMode) taskIdToEdit else (System.currentTimeMillis() % 100000).toInt()

            val task = Task(
                task_id = finalTaskId,
                user_id = currentUserId,
                title = title,
                description = "",
                due_date = dateLong,
                status = "pending",
                priority_id = priorityId
            )

            lifecycleScope.launch {
                if (isEditMode) {
                    db.appDao().updateTask(task)
                    // Reset Notifikasi juga kalau diedit
                    scheduleSpamNotifications(title, dateLong, finalTaskId)
                    Toast.makeText(this@AddTaskActivity, "Tugas Diupdate!", Toast.LENGTH_SHORT).show()
                } else {
                    db.appDao().insertTask(task)
                    scheduleSpamNotifications(title, dateLong, finalTaskId)
                    Toast.makeText(this@AddTaskActivity, "Tugas Disimpan! Siap-siap diingetin.", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }

    private fun showDateTimePicker(editText: EditText) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)

                        val format = "dd-MM-yyyy HH:mm"
                        val sdf = SimpleDateFormat(format, Locale.getDefault())
                        editText.setText(sdf.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // --- LOGIKA SPAM NOTIFIKASI GEN Z (Sesuai Kodemu) ---
    private fun scheduleSpamNotifications(title: String, deadline: Long, baseId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        val spamOffsets = listOf(
            -43200, -21600, -10080, -7200, -4320, -2880, -1440,
            -720, -300, -180, -60, -30, -15, -5, 0,
            60, 120, 1440
        )

        var idOffset = 0

        for (minutes in spamOffsets) {
            val triggerTime = deadline + (minutes * 60 * 1000)

            if (triggerTime > now) {
                val message = when {
                    minutes < 0 -> {
                        val durasi = formatDuration(minutes)
                        "Heh tugas '$title' kamu udah sisa $durasi lagi lo!"
                    }
                    minutes == 0 -> "Heh tugas '$title' DEADLINE SEKARANG WOY!"
                    else -> {
                        val durasi = formatDuration(minutes)
                        "Heh tugas '$title' kamu udh telat $durasi loh! Parah sih."
                    }
                }

                val intent = Intent(this, TaskNotificationReceiver::class.java).apply {
                    putExtra("TASK_TITLE", "Pengingat Tugas 📢")
                    putExtra("TASK_MESSAGE", message)
                    putExtra("TASK_ID", baseId + idOffset)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    this, baseId + idOffset, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
                idOffset++
            }
        }
    }

    private fun formatDuration(minutesInput: Int): String {
        val minutes = abs(minutesInput)
        return when {
            minutes >= 43200 -> "${minutes / 43200} bulan"
            minutes >= 10080 -> "${minutes / 10080} minggu"
            minutes >= 1440 -> "${minutes / 1440} hari"
            minutes >= 60 -> "${minutes / 60} jam"
            else -> "$minutes menit"
        }
    }
}