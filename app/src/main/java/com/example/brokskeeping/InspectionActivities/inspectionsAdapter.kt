package com.example.brokskeeping.InspectionActivities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.Inspection
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R

class InspectionsAdapter(private val inspectionsList: MutableList<Inspection>,
                      private val db: DatabaseHelper,
                      private val inspectionsBrowserActivity: InspectionsBrowserActivity
) : RecyclerView.Adapter<InspectionsAdapter.InspectionViewHolder>() {

    fun updateData(newInspectionsList: List<Inspection>) {
        inspectionsList.clear()
        inspectionsList.addAll(newInspectionsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InspectionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_inspections_browser, parent, false)
        return InspectionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: InspectionViewHolder, position: Int) {
        val currentInspection = inspectionsList[position]
        holder.bind(currentInspection)

        holder.itemView.setOnClickListener {
            inspectionsBrowserActivity.startInspectionDataBrowserActivity(currentInspection.id, currentInspection.stationId)
        }
        holder.itemView.setOnLongClickListener {
            showContextMenu(holder.itemView, currentInspection)
            true
        }
    }

    override fun getItemCount(): Int {
        return inspectionsList.size
    }

    inner class InspectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStationName = itemView.findViewById<TextView>(R.id.tv_station_name)
        private val tvInspectionDate = itemView.findViewById<TextView>(R.id.tv_inspection_date)

        fun bind(inspection: Inspection) {

            val stationName = StationsFunctionality.getStationNameById(db, inspection.stationId)
            tvStationName.text = stationName
            tvInspectionDate.text = inspection.date.toString()
        }
    }

    private fun showContextMenu(view: View, inspection: Inspection) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_long_click_browser, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_long_click_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        view.context.getString(R.string.are_you_sure_you_want_to_delete_this_inspection)
                    ) { confirmed ->
                        if (confirmed) {
                            // User confirmed the deletion
                            InspectionsFunctionality.deleteInspection(db, inspection.id)
                            val (inspections, result) = InspectionsFunctionality.getAllInspections(db)
                            if (result != 1) {
                                Log.e("inspectionsAdapter", "Could not load inspections")
                            }
                            updateData(inspections)
                        }
                    }
                    true
                }
                R.id.menu_long_click_adjust -> {
                    // Handle adjust action
                    inspectionsBrowserActivity.startAdjustInspectionActivity()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
