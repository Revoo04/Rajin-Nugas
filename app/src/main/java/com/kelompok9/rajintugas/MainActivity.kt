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
                    Toast.makeText(this@MainActivity, "Login Berhasil! Halo ${user.username}", Toast.LENGTH_SHORT).show()

                    // Nanti di sini kita pindah ke halaman Dashboard Tugas
                } else {
                    Toast.makeText(this@MainActivity, "Email atau Password salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 4. Logika Tombol DAFTAR (Ini yang baru kita aktifkan!)
        tvRegister.setOnClickListener {
            // Kode ini artinya: "Pindah dari sini (MainActivity) ke RegisterActivity"
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Logika Lupa Password
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword) // Pastikan ID di XML sudah ditambah
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}