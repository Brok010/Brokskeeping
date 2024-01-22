package com.example.brokskeeping

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.Classes.Beehive
import com.example.brokskeeping.Classes.Station

class HivesAdapter(private val hivesList: MutableList<Beehive>,
                   private val stationId: Int,
                   private val stationName: String,
                  private val db: DatabaseHelper,
                  private val hivesBrowserActivity: HivesBrowserActivity
) : RecyclerView.Adapter<HivesAdapter.HiveViewHolder>() {

    fun updateData(newHivesList: List<Beehive>) {
        hivesList.clear()
        hivesList.addAll(newHivesList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiveViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_hive_browser, parent, false)
        return HiveViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HiveViewHolder, position: Int) {
        val currentHive = hivesList[position]
        holder.bind(currentHive)

        holder.itemView.setOnClickListener {
            hivesBrowserActivity.startHiveActivity(stationId, currentHive.id)
        }
        holder.itemView.setOnLongClickListener() {
            showContextMenu(holder.itemView, currentHive)
            true
        }
    }

    override fun getItemCount(): Int {
        return hivesList.size
    }

    inner class HiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStationName: TextView = itemView.findViewById(R.id.tv_value_hives_name)

        fun bind(hive: Beehive) {
            tvStationName.text = hive.id.toString()
        }
    }

    private fun showContextMenu(view: View, hive: Beehive) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_long_click_stations, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_long_click_stations_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        "Are you sure you want to delete this hive?"
                    ) { confirmed ->
                        if (confirmed) {
                            // User confirmed the deletion
                            db.deleteHive(stationId, hive.id)
                            updateData(db.getAllHives(stationId))
                        }
                    }
                    true
                }
                R.id.menu_long_click_stations_adjust -> {
                    // Handle adjust action
                    hivesBrowserActivity.startAdjustHiveActivity(stationId, hive.id)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
