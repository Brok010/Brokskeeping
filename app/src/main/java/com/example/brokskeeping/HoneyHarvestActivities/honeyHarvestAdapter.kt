package com.example.brokskeeping.HoneyHarvestActivities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.HoneyHarvest
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.R


class HoneyHarvestAdapter(private val honeyHarvestList: MutableList<Pair<Int, Int>>,
                          private var type: String,
                         private val db: DatabaseHelper,
                         private val honeyHarvestBrowserActivity: HoneyHarvestBrowserActivity
) : RecyclerView.Adapter<HoneyHarvestAdapter.HoneyHarvestViewHolder>() {

    fun updateData(newHoneyHarvestList: List<Pair<Int, Int>>, type: String) {
        honeyHarvestList.clear()
        honeyHarvestList.addAll(newHoneyHarvestList)
        this.type = type
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoneyHarvestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_honey_harvest_browser, parent, false)
        return HoneyHarvestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HoneyHarvestViewHolder, position: Int) {
        val currentHoneyHarvest = honeyHarvestList[position]
        holder.bind(currentHoneyHarvest, type)

        holder.itemView.setOnClickListener {
            //
        }
    }

    override fun getItemCount(): Int {
        return honeyHarvestList.size
    }

    inner class HoneyHarvestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLocation = itemView.findViewById<TextView>(R.id.tv_location)
        private val tvHoneyFramesHarvested = itemView.findViewById<TextView>(R.id.tv_honey_harvested)

        fun bind(honeyHarvest: Pair<Int, Int>, type: String) {
            if (type == "Station") {
                val entityName = StationsFunctionality.getStationNameById(db, honeyHarvest.first)
                tvLocation.text = entityName
            } else {
                val entityName = HivesFunctionality.getHiveNameById(db, honeyHarvest.first)
                tvLocation.text = entityName
                setStationName(honeyHarvest)
            }
            tvHoneyFramesHarvested.text = honeyHarvest.second.toString()
        }

        private fun setStationName(honeyHarvest: Pair<Int, Int>) {
            val context = itemView.context
            val stationId = HivesFunctionality.getStationIdByHiveId(db, honeyHarvest.first)
            val station = StationsFunctionality.getStationsAttributes(db, stationId)

            val llDynamic = itemView.findViewById<ViewGroup>(R.id.ll_dynamic)
            llDynamic.removeAllViews()

            val tvLabel = TextView(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 4.dpToPx(context)
                }
                text = "Station: "
                setTextColor(context.getColor(R.color.basicTextColor))
            }

            val tvStationName = TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                if (station != null) {
                    text = station.name
                }
                setTextColor(context.getColor(R.color.basicTextColor))
            }

            llDynamic.addView(tvLabel)
            llDynamic.addView(tvStationName)
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
