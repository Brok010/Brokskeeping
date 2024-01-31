package com.example.brokskeeping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.Classes.HumTempData

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
            //
        }

        holder.itemView.setOnLongClickListener() {
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
            tvLogsDateRange.text = "${log.firstDate} to ${log.lastDate}"
        }
    }
    private fun showContextMenu(view: View, log: HumTempData) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_long_click_browser, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_long_click_stations_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        "Are you sure you want to delete this log?"
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
