package com.kelompok9.rajintugas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.kelompok9.rajintugas.data.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. JURUS ANTI GELAP (Force Light Mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // 2. JURUS ANTI LUPA (Cek Session Login) - BAGIAN BARU
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val savedId = sharedPref.getInt("USER_ID", -1)

        // Kalau ID ditemukan (artinya user sudah pernah login dan belum logout)
        if (savedId != -1) {
            // Langsung pindah ke Dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
            finish() // Tutup halaman login biar gak balik lagi
            return // Stop kode di bawahnya, jangan load layout login
        }

        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        // Karena kita pakai TextInputEditText di XML, casting ke EditText tetap aman
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi email dan password dulu ya!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = db.appDao().loginUser(email, password)

                if (user != null) {
                    Toast.makeText(this@MainActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                    // Simpan Data User ke Saku (Session)
                    val editor = sharedPref.edit()
                    editor.putInt("USER_ID", user.user_id)
                    editor.putString("USERNAME", user.username)
                    editor.putString("EMAIL", user.email)
                    editor.apply()

                    // Pindah ke Dashboard
                    startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Email atau Password salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}