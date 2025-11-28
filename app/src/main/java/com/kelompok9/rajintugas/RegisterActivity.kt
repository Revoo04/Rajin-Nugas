package com.kelompok9.rajintugas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kelompok9.rajintugas.data.AppDatabase
import com.kelompok9.rajintugas.data.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Inisialisasi Database
        val db = AppDatabase.getDatabase(this)

        // 2. Kenalan sama komponen layout
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmailReg)
        val etPassword = findViewById<EditText>(R.id.etPasswordReg)
        val etConfirmPass = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)
        val ivBack = findViewById<ImageView>(R.id.ivBack)

        // 3. Aksi Tombol DAFTAR
        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPass = etConfirmPass.text.toString()

            // Validasi Input
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPass) {
                Toast.makeText(this, "Konfirmasi password tidak sama!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan ke Database
            val newUser = User(
                username = name,
                email = email,
                password = password,
                role = "user"
            )

            lifecycleScope.launch {
                db.appDao().registerUser(newUser)
                Toast.makeText(this@RegisterActivity, "Akun berhasil dibuat! Silakan Login.", Toast.LENGTH_LONG).show()
                finish() // Menutup halaman Register dan kembali ke Login
            }
        }

        // 4. Aksi tombol "Sudah punya akun? Masuk"
        tvLoginLink.setOnClickListener {
            finish() // Kembali ke Login
        }

        ivBack.setOnClickListener {
            finish()
        }
    }
}