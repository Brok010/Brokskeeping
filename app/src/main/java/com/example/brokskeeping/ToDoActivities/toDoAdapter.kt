package com.example.brokskeeping.ToDoActivities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.ToDo
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R
import java.text.SimpleDateFormat
import java.util.Locale

class ToDoAdapter(private val toDosList: MutableList<ToDo>,
                  private val db: DatabaseHelper,
                  private val toDosBrowserActivity: ToDoBrowserActivity,
                  private val hiveId: Int,
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    fun updateData(newToDosList: List<ToDo>) {
        toDosList.clear()
        toDosList.addAll(newToDosList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_to_do_browser, parent, false)
        return ToDoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val currentToDo = toDosList[position]
        holder.bind(currentToDo)

        holder.itemView.setOnClickListener {
            //
        }
        holder.itemView.setOnLongClickListener() {
            showContextMenu(holder.itemView, currentToDo)
            true
        }
    }

    override fun getItemCount(): Int {
        return toDosList.size
    }

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTodoDate: TextView = itemView.findViewById(R.id.tv_to_do_date)
        private val tvTodoText: TextView = itemView.findViewById(R.id.tv_to_do_text)

        fun bind(toDo: ToDo) {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) // Customize the pattern as needed
            val formattedDate = dateFormat.format(toDo.date)

            tvTodoDate.text = formattedDate
            tvTodoText.text = toDo.toDoText
        }
    }
    private fun showContextMenu(view: View, toDo: ToDo) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_long_click_browser, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_long_click_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        "Are you sure you want to delete this To Do?"
                    ) { confirmed ->
                        if (confirmed) {
                            // User confirmed the deletion
                            ToDoFunctionality.deleteToDo(db, toDo.id)
                            updateData(ToDoFunctionality.getAllToDos(db, hiveId))
                        }
                    }

                    true
                }
                R.id.menu_long_click_adjust -> {
                    // Handle adjust action
                    toDosBrowserActivity.startAdjustToDoActivity(toDo.id)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
