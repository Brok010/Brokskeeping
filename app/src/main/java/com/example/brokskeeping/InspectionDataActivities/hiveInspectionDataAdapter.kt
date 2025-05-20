package com.example.brokskeeping.InspectionDataActivities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.InspectionData
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R

class HiveInspectionDataAdapter(private val hiveInspectionDataList: MutableList<InspectionData>,
                            private val hiveId: Int,
                            private val db: DatabaseHelper,
                            private val hiveInspectionDataBrowserActivity: HiveInspectionDataBrowserActivity,
                            private val dataChangedListener: OnDataChangedListener
) : RecyclerView.Adapter<HiveInspectionDataAdapter.HiveInspectionDataViewHolder>() {

    interface OnDataChangedListener {
        fun onDataChanged()
    }
    fun updateData(newHiveInspectionDataList: List<InspectionData>) {
        hiveInspectionDataList.clear()
        hiveInspectionDataList.addAll(newHiveInspectionDataList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiveInspectionDataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_inspection_data_browser, parent, false)
        return HiveInspectionDataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HiveInspectionDataViewHolder, position: Int) {
        val currentInspectionData = hiveInspectionDataList[position]
        holder.bind(currentInspectionData)

        holder.itemView.setOnClickListener {
            hiveInspectionDataBrowserActivity.startInspectionDataActivity(currentInspectionData.id)
        }
        holder.itemView.setOnLongClickListener {
            showContextMenu(holder.itemView, currentInspectionData)
            true
        }
    }

    override fun getItemCount(): Int {
        return hiveInspectionDataList.size
    }

    inner class HiveInspectionDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHiveName = itemView.findViewById<TextView>(R.id.tv_hive_name)
        private val tvFreeFrames = itemView.findViewById<TextView>(R.id.tv_free_frames)
        private val tvInspectionNotes = itemView.findViewById<TextView>(R.id.tv_inspection_notes)
        private val tvDate = itemView.findViewById<TextView>(R.id.tv_date)

        fun bind(inspectionData: InspectionData) {
            val (inspection, result) = InspectionsFunctionality.getInspection(db, inspectionData.inspectionId)
            if (result == 0) {
                Log.e("inspectionDataAdapter", "InspectionsFunctionality.getInspection returned 0")
            }
            val hiveName = HivesFunctionality.getHiveNameById(db, inspectionData.hiveId )
            val hiveNotes = NotesFunctionality.getNote(db, inspectionData.noteId)
            tvHiveName.text = hiveName
            tvFreeFrames.text = inspectionData.freeSpaceFrames.toString()
            tvInspectionNotes.text = hiveNotes.noteText
            tvDate.text = inspection!!.date.toString()
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
                            InspectionsFunctionality.deleteInspectionData(db, inspectionData.id)
                            dataChangedListener.onDataChanged()
                        }
                    }
                    true
                }
                R.id.menu_long_click_adjust -> {
                    // Handle adjust action
                    hiveInspectionDataBrowserActivity.startAdjustInspectionDataActivity()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
