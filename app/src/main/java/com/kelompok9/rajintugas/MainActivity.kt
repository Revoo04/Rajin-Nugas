package com.kelompok9.rajintugas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kelompok9.rajintugas.data.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inisialisasi Database
        db = AppDatabase.getDatabase(this)

        // 2. Hubungkan dengan komponen di Layout
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // 3. Logika Tombol MASUK (Login)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi email dan password dulu ya!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek ke Database apakah user ada
            lifecycleScope.launch {
                val user = db.appDao().loginUser(email, password)

                if (user != null) {
                    Toast.makeText(this@MainActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                    // --- BAGIAN INI YANG KITA UBAH ---
                    // Pindah ke Dashboard & Kirim Nama User
                    val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                    intent.putExtra("USERNAME", user.username) // Kirim nama biar bisa disapa
                    startActivity(intent)
                    finish() // Tutup halaman Login biar gak bisa balik lagi
                    // ----------------------------------

                } else {
                    Toast.makeText(this@MainActivity, "Email atau Password salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 4. Logika Tombol DAFTAR
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 5. Logika Lupa Password
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}