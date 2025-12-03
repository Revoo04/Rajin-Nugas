package com.kelompok9.rajintugas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog // Import Alert Dialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnBack = findViewById<ImageView>(R.id.btnBackProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)

        // 1. TANGKAP DATA ASLI (Dari Saku/SharedPreferences)
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Pengguna")
        val email = sharedPref.getString("EMAIL", "email@contoh.com")

        // 2. TAMPILKAN DI LAYAR
        tvName.text = username
        tvEmail.text = email

        btnBack.setOnClickListener {
            finish() // Kembali ke Dashboard
        }

        // 3. LOGIKA LOGOUT (Versi Gen Z)
        btnLogout.setOnClickListener {
            // Tampilkan Dialog Konfirmasi Dulu
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Yakin mau cabut? 🏃💨")
            builder.setMessage("Serius nih mau logout? Nanti kangen loh sama tugas-tugasnya... 🥺")

            // Tombol YA
            builder.setPositiveButton("Gas, Logout!") { _, _ ->
                // Hapus Data di Saku
                val editor = sharedPref.edit()
                editor.clear()
                editor.apply()

                // Tendang ke Halaman Login
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

            // Tombol TIDAK
            builder.setNegativeButton("Gajadi deh") { dialog, _ ->
                dialog.dismiss() // Tutup dialog, stay di profil
            }

            builder.show()
        }
    }
}