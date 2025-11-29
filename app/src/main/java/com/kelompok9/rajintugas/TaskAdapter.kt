package com.kelompok9.rajintugas

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

class TaskAdapter(
    private val onDeleteClick: (Task) -> Unit,
    private val onStatusChange: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var taskList = emptyList<Task>()

    fun setData(tasks: List<Task>) {
        this.taskList = tasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Gunakan layout item_task yang baru
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
        // Ini komponen Garis Warna yang baru
        val viewPriorityColor: View = itemView.findViewById(R.id.viewPriorityColor)

        fun bind(task: Task) {
            tvTitle.text = task.title

            // Format Tanggal
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            tvDate.text = formatter.format(task.due_date)

            // Status Checkbox (Disembunyikan sementara sesuai desain, tapi logikanya tetap ada)
            cbDone.isChecked = (task.status == "done")
            cbDone.setOnCheckedChangeListener { _, isChecked ->
                onStatusChange(task, isChecked)
            }

            // --- LOGIKA WARNA PRIORITAS (Garis Kiri) ---
            // 1=Penting (Merah), 2=Sedang (Kuning/Oranye), 3=Santai (Hijau)
            when (task.priority_id) {
                1 -> { // Penting -> Merah
                    viewPriorityColor.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                }
                2 -> { // Sedang -> Kuning Emas (Mirip desain Adji)
                    viewPriorityColor.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_light))
                }
                else -> { // Santai -> Hijau
                    viewPriorityColor.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                }
            }
        }
    }
}