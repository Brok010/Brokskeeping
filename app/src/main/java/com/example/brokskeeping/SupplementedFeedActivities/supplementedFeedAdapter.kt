package com.example.brokskeeping.SupplementedFeedActivities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.R


class SupplementedFeedAdapter(private val supplementedFeedList: MutableList<Pair<Int, Int>>,
                          private var type: String,
                          private val db: DatabaseHelper,
                          private val supplementedFeedBrowserActivity: SupplementedFeedBrowserActivity
) : RecyclerView.Adapter<SupplementedFeedAdapter.SupplementedFeedViewHolder>() {

    fun updateData(newSupplementedFeedList: List<Pair<Int, Int>>, type: String) {
        supplementedFeedList.clear()
        supplementedFeedList.addAll(newSupplementedFeedList)
        this.type = type
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplementedFeedViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_supplemented_feed_browser, parent, false)
        return SupplementedFeedViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SupplementedFeedViewHolder, position: Int) {
        val currentSupplementedFeed = supplementedFeedList[position]
        holder.bind(currentSupplementedFeed, type)

        holder.itemView.setOnClickListener {
            //
        }
    }

    override fun getItemCount(): Int {
        return supplementedFeedList.size
    }

    inner class SupplementedFeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLocation = itemView.findViewById<TextView>(R.id.tv_location)
        private val tvHoneyFramesHarvested = itemView.findViewById<TextView>(R.id.tv_honey_harvested)

        fun bind(supplementedFeed: Pair<Int, Int>, type: String) {
            if (type == itemView.context.getString(R.string.station)) {
                val entityName = StationsFunctionality.getStationNameById(db, supplementedFeed.first)
                tvLocation.text = entityName
            } else {
                val entityName = HivesFunctionality.getHiveNameById(db, supplementedFeed.first)
                tvLocation.text = entityName
                setStationName(supplementedFeed)
            }
            tvHoneyFramesHarvested.text = supplementedFeed.second.toString()
        }

        private fun setStationName(supplementedFeed: Pair<Int, Int>) {
            val context = itemView.context
            val stationId = HivesFunctionality.getStationIdByHiveId(db, supplementedFeed.first)
            val (station, stationResult) = StationsFunctionality.getStationsAttributes(db, stationId)
            if (stationResult == 0) {
                Log.e("supplementedFeedAdapter", "Couldn't retrieve station")
            }
            val llDynamic = itemView.findViewById<ViewGroup>(R.id.ll_dynamic)
            llDynamic.removeAllViews()

            val tvLabel = TextView(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 4.dpToPx(context)
                }
                text = context.getString(R.string.invalid_text)
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
