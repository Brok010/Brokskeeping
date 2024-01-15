package com.example.brokskeeping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.Classes.Beehive

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
}
