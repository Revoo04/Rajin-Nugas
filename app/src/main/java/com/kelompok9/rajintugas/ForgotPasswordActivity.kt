package com.kelompok9.rajintugas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kelompok9.rajintugas.data.AppDatabase
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val db = AppDatabase.getDatabase(this)

        // Komponen UI
        val etEmail = findViewById<EditText>(R.id.etEmailReset)
        val etNewPass = findViewById<EditText>(R.id.etNewPassword)
        val btnReset = findViewById<Button>(R.id.btnReset)
        val btnBack = findViewById<ImageView>(R.id.btnBackForgot) // Tombol Kembali

        // 1. LOGIKA TOMBOL KEMBALI
        btnBack.setOnClickListener {
            finish() // Tutup halaman ini, balik ke Login
        }

        // 2. LOGIKA RESET PASSWORD
        btnReset.setOnClickListener {
            val email = etEmail.text.toString()
            val newPass = etNewPass.text.toString()

            // Validasi Input
            if (email.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Isi semua data dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi Panjang Password (Biar konsisten sama Register)
            if (newPass.length < 8) {
                Toast.makeText(this, "Password minimal 8 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Mencoba update password di database
                val rowsAffected = db.appDao().resetPassword(email, newPass)

                if (rowsAffected > 0) {
                    // Jika sukses (email ditemukan)
                    Toast.makeText(this@ForgotPasswordActivity, "Password berhasil diubah! Silakan Login.", Toast.LENGTH_LONG).show()
                    finish() // Balik ke Login otomatis
                } else {
                    // Jika gagal (email tidak terdaftar)
                    Toast.makeText(this@ForgotPasswordActivity, "Email tidak terdaftar!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}