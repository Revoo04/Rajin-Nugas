package com.kelompok9.rajintugas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
        val etEmail = findViewById<EditText>(R.id.etEmailReset)
        val etNewPass = findViewById<EditText>(R.id.etNewPassword)
        val btnReset = findViewById<Button>(R.id.btnReset)

        btnReset.setOnClickListener {
            val email = etEmail.text.toString()
            val newPass = etNewPass.text.toString()

            if (email.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Isi semua data dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Mencoba update password
                val rowsAffected = db.appDao().resetPassword(email, newPass)

                if (rowsAffected > 0) {
                    // Jika ada baris yang berubah, berarti email ditemukan & sukses
                    Toast.makeText(this@ForgotPasswordActivity, "Password berhasil diubah! Silakan Login.", Toast.LENGTH_LONG).show()
                    finish() // Balik ke Login
                } else {
                    // Jika 0, berarti email tidak ada di database
                    Toast.makeText(this@ForgotPasswordActivity, "Email tidak terdaftar!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}