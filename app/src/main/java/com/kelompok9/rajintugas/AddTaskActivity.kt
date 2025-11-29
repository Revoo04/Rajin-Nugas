package com.kelompok9.rajintugas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kelompok9.rajintugas.data.AppDatabase
import com.kelompok9.rajintugas.data.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // 1. Siapkan Database
        val db = AppDatabase.getDatabase(this)

        // 2. Hubungkan Komponen
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDate = findViewById<EditText>(R.id.etDate)
        val rgPriority = findViewById<RadioGroup>(R.id.rgPriority)
        val btnSave = findViewById<Button>(R.id.btnSaveTask)

        // 3. Aksi Tombol Simpan
        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val dateStr = etDate.text.toString()

            // Validasi: Tidak boleh kosong
            if (title.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "Isi judul dan tanggal dulu ya!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tentukan Angka Prioritas (1=Penting, 2=Sedang, 3=Santai)
            val priorityId = when (rgPriority.checkedRadioButtonId) {
                R.id.rbHigh -> 1
                R.id.rbMedium -> 2
                else -> 3
            }

            // Ubah Format Tanggal (String -> Long) biar database ngerti
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val dateLong = try {
                formatter.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis() // Kalau format salah, pakai tanggal hari ini
            }

            // Bungkus data jadi Object Task
            val newTask = Task(
                user_id = 1, // Sementara kita set user 1
                title = title,
                description = "",
                due_date = dateLong,
                status = "pending", // Defaultnya belum selesai
                priority_id = priorityId
            )

            // Simpan ke Database (Background Process)
            lifecycleScope.launch {
                db.appDao().insertTask(newTask)
                Toast.makeText(this@AddTaskActivity, "Tugas Berhasil Disimpan!", Toast.LENGTH_SHORT).show()

                // Tutup halaman ini, balik ke Dashboard/List
                finish()
            }
        }
    }
}