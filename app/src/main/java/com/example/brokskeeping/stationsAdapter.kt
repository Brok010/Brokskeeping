package com.example.brokskeeping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.Classes.Station

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
    }

    override fun getItemCount(): Int {
        return stationsList.size
    }

    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStationName: TextView = itemView.findViewById(R.id.tv_value_stations_name)
        private val tvHiveCount: TextView = itemView.findViewById(R.id.tv_value_hive_count)

        fun bind(station: Station) {
            tvStationName.text = station.name
            tvHiveCount.text = db.getHiveCount(station.id).toString()
        }
    }
}
