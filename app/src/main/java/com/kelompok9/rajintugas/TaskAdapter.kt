package com.kelompok9.rajintugas

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kelompok9.rajintugas.data.Task
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class TaskAdapter(
    // 2 Parameter Wajib: Status Change & Item Click (Long Press)
    private val onStatusChange: (Task, Boolean) -> Unit,
    private val onDeleteClick: (Task) -> Unit // Ini kita pakai buat Klik Tahan
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var taskList = emptyList<Task>()

    fun setData(tasks: List<Task>) {
        this.taskList = tasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = taskList.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val cbDone: CheckBox = itemView.findViewById(R.id.cbDone)
        val viewPriorityColor: View = itemView.findViewById(R.id.viewPriorityColor)

        fun bind(task: Task) {
            tvTitle.text = task.title

            // 1. FORMAT TANGGAL ASLI
            val formatter = SimpleDateFormat("EEE, dd MMM • HH:mm", Locale("id", "ID"))
            val originalDateStr = formatter.format(task.due_date)

            // 2. HITUNG MUNDUR REALTIME (Menit & Detik)
            val now = System.currentTimeMillis()
            val diff = task.due_date - now

            var statusStr = ""

            if (task.status == "done") {
                statusStr = "(Selesai)"
                tvDate.setTextColor(Color.parseColor("#4CAF50")) // Hijau
                tvDate.typeface = Typeface.DEFAULT
            }
            else if (diff > 0) {
                // Masih ada waktu
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

                statusStr = if (days > 0) {
                    "(Sisa: $days hr $hours jam)"
                } else {
                    // Tampilkan menit & detik kalau < 24 jam
                    String.format("(Sisa: %02d jam %02d mnt %02d dtk)", hours, minutes, seconds)
                }

                if (days == 0L && hours == 0L) {
                    tvDate.setTextColor(Color.parseColor("#FF9800")) // Oranye (Mepet)
                    tvDate.typeface = Typeface.DEFAULT_BOLD
                } else {
                    tvDate.setTextColor(Color.parseColor("#757575")) // Abu (Aman)
                    tvDate.typeface = Typeface.DEFAULT
                }
            }
            else {
                // Telat
                val absDiff = abs(diff)
                val days = TimeUnit.MILLISECONDS.toDays(absDiff)
                val hours = TimeUnit.MILLISECONDS.toHours(absDiff) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(absDiff) % 60

                statusStr = if (days > 0) {
                    "(Telat $days hr $hours jam!)"
                } else {
                    "(Telat $hours jam $minutes mnt!)"
                }

                tvDate.setTextColor(Color.RED) // Merah
                tvDate.typeface = Typeface.DEFAULT_BOLD
            }

            // Gabungkan teks
            tvDate.text = "$originalDateStr\n$statusStr"

            // 3. LISTENERS

            // Checkbox
            cbDone.setOnCheckedChangeListener(null)
            cbDone.isChecked = (task.status == "done")
            cbDone.setOnCheckedChangeListener { _, isChecked ->
                onStatusChange(task, isChecked)
            }

            // KLIK TAHAN (Long Press) buat Edit/Hapus
            itemView.setOnLongClickListener {
                onDeleteClick(task) // Panggil fungsi di Activity
                true
            }

            // Warna Prioritas
            when (task.priority_id) {
                1 -> viewPriorityColor.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                2 -> viewPriorityColor.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_light))
                else -> viewPriorityColor.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
            }
        }
    }
}