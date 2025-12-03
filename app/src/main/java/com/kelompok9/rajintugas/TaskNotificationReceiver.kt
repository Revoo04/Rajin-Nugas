package com.kelompok9.rajintugas

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class TaskNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Pengingat"
        val taskMessage = intent.getStringExtra("TASK_MESSAGE") ?: "Cek tugasmu sekarang!"
        val taskId = intent.getIntExtra("TASK_ID", 0)

        showAlarmNotification(context, taskTitle, taskMessage, taskId)
    }

    private fun showAlarmNotification(context: Context, title: String, message: String, taskId: Int) {
        // Ganti ID Channel biar settingan lama kereset
        val channelId = "task_alarm_channel_v2"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ambil Suara Alarm (Kringgg!)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // 1. Setup Channel (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Cek apakah channel sudah ada
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, "Alarm Tugas Keras", NotificationManager.IMPORTANCE_HIGH)
                channel.description = "Notifikasi Berisik untuk Deadline"
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(0, 1000, 500, 1000) // Getar: Diam-Zzzzt-Diam-Zzzzt

                // Set Suara ke Audio Alarm (Biar tetap bunyi walau media silent)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                channel.setSound(alarmSound, audioAttributes)

                notificationManager.createNotificationChannel(channel)
            }
        }

        // 2. Intent ke Dashboard
        val intent = Intent(context, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // 3. Desain Notifikasi
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX) // Prioritas Maksimal
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Kategori Alarm
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setOngoing(true) // Susah di-swipe (harus diklik)

        val notification = builder.build()

        // --- JURUS RAHASIA: FLAG_INSISTENT ---
        // Ini yang bikin suaranya ngulang-ngulang terus kayak telpon masuk
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        notificationManager.notify(taskId, notification)
    }
}