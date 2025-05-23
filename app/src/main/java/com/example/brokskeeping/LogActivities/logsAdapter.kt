package com.example.brokskeeping.LogActivities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.HumTempData
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HumTempDataFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R
import java.text.SimpleDateFormat
import java.util.Locale

class LogsAdapter(private val logsList: MutableList<HumTempData>,
                  private val hiveId: Int,
                  private val db: DatabaseHelper,
                  private val logsBrowserActivity: LogsBrowserActivity
) : RecyclerView.Adapter<LogsAdapter.HumTempDataViewHolder>() {

    fun updateData(newLogsList: List<HumTempData>) {
        logsList.clear()
        logsList.addAll(newLogsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HumTempDataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_log_browser, parent, false)
        return HumTempDataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HumTempDataViewHolder, position: Int) {
        val currentHumTempData = logsList[position]
        holder.bind(currentHumTempData)

        holder.itemView.setOnClickListener {
            logsBrowserActivity.startLogActivity(currentHumTempData.id)
        }

        holder.itemView.setOnLongClickListener {
            showContextMenu(holder.itemView, currentHumTempData)
            true
        }
    }

    override fun getItemCount(): Int {
        return logsList.size
    }

    inner class HumTempDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLogsDateRange: TextView = itemView.findViewById(R.id.tv_date_range)

        fun bind(log: HumTempData) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
            val formattedFirstDate = dateFormat.format(log.firstDate)
            val formattedLastDate = dateFormat.format(log.lastDate)

            tvLogsDateRange.text = itemView.context.getString(
                R.string.formatted_first_date_to_formatted_last_date,
                formattedFirstDate,
                formattedLastDate
            )
        }
    }
    private fun showContextMenu(view: View, log: HumTempData) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_delete, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        view.context.getString(R.string.are_you_sure_you_want_to_delete_this_log)
                    ) { confirmed ->
                        if (confirmed) {
                            // User confirmed the deletion
                            HumTempDataFunctionality.deleteHumTempData(db, log.id)
                            updateData(HumTempDataFunctionality.getAllHumTempData(db, hiveId))
                        }
                    }
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
