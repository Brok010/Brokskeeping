package com.example.brokskeeping.DiscardedEntitiesActivities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DataClasses.DiscardedEntity
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.R
import java.text.SimpleDateFormat
import java.util.Locale


class DiscardedEntitiesAdapter(private val discardedEntitiesList: MutableList<DiscardedEntity>,
                               private val db: DatabaseHelper,
                               private val discardedEntitiesBrowserActivity: DiscardedEntitiesBrowserActivity
) : RecyclerView.Adapter<DiscardedEntitiesAdapter.DiscardedEntitiesViewHolder>() {

    fun updateData(newDiscardedEntitiesList: List<DiscardedEntity>) {
        discardedEntitiesList.clear()
        discardedEntitiesList.addAll(newDiscardedEntitiesList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscardedEntitiesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_discarded_entity_browser, parent, false)
        return DiscardedEntitiesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DiscardedEntitiesViewHolder, position: Int) {
        val currentDiscardedEntity = discardedEntitiesList[position]
        holder.bind(currentDiscardedEntity)
        val activity = DiscardedEntitiesBrowserActivity()

        holder.itemView.setOnClickListener {
            when (currentDiscardedEntity) {
                is DiscardedEntity.DiscardedHive -> activity.startHiveActivity(currentDiscardedEntity.hive.id)
                is DiscardedEntity.DiscardedStation -> activity.startStationActivity(currentDiscardedEntity.station.id)
            }
        }
    }


    override fun getItemCount(): Int {
        return discardedEntitiesList.size
    }

    inner class DiscardedEntitiesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)
        private val tvCreation: TextView = itemView.findViewById(R.id.tv_creation)
        private val tvDeath: TextView = itemView.findViewById(R.id.tv_death)

        fun bind(discardedEntity: DiscardedEntity) {
            val sdf = SimpleDateFormat("MM/yyyy", Locale.getDefault())
            when (discardedEntity) {
                is DiscardedEntity.DiscardedHive -> {
                    tvName.text = discardedEntity.hive.nameTag
                    tvType.text = "Hive"
                    tvCreation.text = sdf.format(discardedEntity.hive.creationTime)
                    tvDeath.text = sdf.format(discardedEntity.hive.deathTime)
                }

                is DiscardedEntity.DiscardedStation -> {
                    tvName.text = discardedEntity.station.name
                    tvType.text = "Station"
                    tvCreation.text = sdf.format(discardedEntity.station.creationTime)
                    tvDeath.text = sdf.format(discardedEntity.station.deathTime)
                }
            }
        }
    }
}
