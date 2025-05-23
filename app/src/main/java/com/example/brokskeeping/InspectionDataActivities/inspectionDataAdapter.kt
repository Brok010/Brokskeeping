package com.example.brokskeeping.InspectionDataActivities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.InspectionData
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R

class InspectionDataAdapter(private val inspectionDataList: MutableList<InspectionData>,
                      private val inspectionId: Int,
                      private val db: DatabaseHelper,
                      private val inspectionDataBrowserActivity: InspectionDataBrowserActivity
) : RecyclerView.Adapter<InspectionDataAdapter.InspectionDataViewHolder>() {

    fun updateData(newInspectionDataList: List<InspectionData>) {
        inspectionDataList.clear()
        inspectionDataList.addAll(newInspectionDataList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InspectionDataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_inspection_data_browser, parent, false)
        return InspectionDataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: InspectionDataViewHolder, position: Int) {
        val currentInspectionData = inspectionDataList[position]
        holder.bind(currentInspectionData)

        holder.itemView.setOnClickListener {
            inspectionDataBrowserActivity.startInspectionDataActivity(currentInspectionData.id, inspectionId)
        }
        holder.itemView.setOnLongClickListener {
            showContextMenu(holder.itemView, currentInspectionData)
            true
        }
    }

    override fun getItemCount(): Int {
        return inspectionDataList.size
    }

    inner class InspectionDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val llDate = itemView.findViewById<View>(R.id.ll_date)
        private val tvHiveName = itemView.findViewById<TextView>(R.id.tv_hive_name)
        private val tvFreeFrames = itemView.findViewById<TextView>(R.id.tv_free_frames)
        private val tvInspectionNotes = itemView.findViewById<TextView>(R.id.tv_inspection_notes)
//        private val tvDate = itemView.findViewById<TextView>(R.id.tv_date)

        fun bind(inspectionData: InspectionData) {
//            val (inspection, result) = InspectionsFunctionality.getInspection(db, inspectionData.inspectionId)
//            if (result == 0) {
//                Log.e("inspectionDataAdapter", "InspectionsFunctionality.getInspection returned 0")
//            }
            llDate.visibility = View.GONE
            val hiveName = HivesFunctionality.getHiveNameById(db, inspectionData.hiveId )
            val hiveNotes = NotesFunctionality.getNote(db, inspectionData.noteId)
            tvHiveName.text = hiveName
            tvFreeFrames.text = inspectionData.freeSpaceFrames.toString()
            tvInspectionNotes.text = hiveNotes.noteText
//            tvDate.text = inspection!!.date.toString()
        }
    }


    private fun showContextMenu(view: View, inspectionData: InspectionData) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_long_click_browser, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_long_click_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        view.context.getString(R.string.are_you_sure_you_want_to_delete_this_inspection_data)
                    ) { confirmed ->
                        if (confirmed) {
                            // User confirmed the deletion
                            InspectionsFunctionality.deleteInspectionData(db, inspectionData.id)
                            val (inspectionData, result) = InspectionsFunctionality.getAllInspectionDataForInspectionId(db, inspectionId)
                            if (result == 1) {
                                updateData(inspectionData)
                            }
                        }
                    }
                    true
                }
                R.id.menu_long_click_adjust -> {
                    // Handle adjust action
                    inspectionDataBrowserActivity.startAdjustInspectionDataActivity()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
