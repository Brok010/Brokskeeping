package com.example.brokskeeping.StationActivities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R

class StationsAdapter(private val stationsList: MutableList<Station>,
                      private val db: DatabaseHelper,
                      private val stationsBrowserActivity: StationsBrowserActivity
) : RecyclerView.Adapter<StationsAdapter.StationViewHolder>() {

    fun updateData(newStationsList: List<Station>) {
        stationsList.clear()
        stationsList.addAll(newStationsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_station_browser, parent, false)
        return StationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val currentStation = stationsList[position]
        holder.bind(currentStation)

        holder.itemView.setOnClickListener {
            stationsBrowserActivity.startBrowseHivesActivity(currentStation.name, currentStation.id)
        }
        holder.itemView.setOnLongClickListener() {
            showContextMenu(holder.itemView, currentStation)
            true
        }
    }

    override fun getItemCount(): Int {
        return stationsList.size
    }

    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStationName: TextView = itemView.findViewById(R.id.tv_value_stations_name)
        private val tvStationLocation: TextView = itemView.findViewById(R.id.tv_value_stations_location)
        private val tvHiveCount: TextView = itemView.findViewById(R.id.tv_value_hive_count)

        fun bind(station: Station) {
            tvStationName.text = station.name
            tvStationLocation.text = station.location
            tvHiveCount.text = StationsFunctionality.getHiveCount(db, station.id).toString()
        }
    }
    private fun showContextMenu(view: View, station: Station) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_long_click_browser, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_long_click_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        "Are you sure you want to delete this station?"
                    ) { confirmed ->
                        if (confirmed) {
                            // User confirmed the deletion
                            StationsFunctionality.deleteStation(db, station.id)
                            updateData(StationsFunctionality.getAllStations(db))
                        }
                    }

                    true
                }
                R.id.menu_long_click_adjust -> {
                    // Handle adjust action
                    stationsBrowserActivity.startAdjustStationActivity(station.id)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
