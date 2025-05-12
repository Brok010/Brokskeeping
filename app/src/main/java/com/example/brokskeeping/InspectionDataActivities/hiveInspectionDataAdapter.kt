//package com.example.brokskeeping.HiveInspectionDataActivities
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.PopupMenu
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.brokskeeping.DataClasses.InspectionData
//import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
//import com.example.brokskeeping.DbFunctionality.DatabaseHelper
//import com.example.brokskeeping.DbFunctionality.HivesFunctionality
//import com.example.brokskeeping.Functionality.Utils
//import com.example.brokskeeping.R
//
//class HiveInspectionDataAdapter(private val inspectionDataList: MutableList<InspectionData>,
//                                private val inspectionId: Int,
//                                private val db: DatabaseHelper,
//                                private val inspectionDataBrowserActivity: HiveInspectionDataBrowserActivity
//) : RecyclerView.Adapter<HiveInspectionDataAdapter.HiveInspectionDataViewHolder>() {
//
//    fun updateData(newHiveInspectionDataList: List<HiveInspectionData>) {
//        inspectionDataList.clear()
//        inspectionDataList.addAll(newHiveInspectionDataList)
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiveInspectionDataViewHolder {
//        val itemView = LayoutInflater.from(parent.context)
//            .inflate(R.layout.view_inspection_data_browser, parent, false)
//        return HiveInspectionDataViewHolder(itemView)
//    }
//
//    override fun onBindViewHolder(holder: HiveInspectionDataViewHolder, position: Int) {
//        val currentHiveInspectionData = inspectionDataList[position]
//        holder.bind(currentHiveInspectionData)
//
//        holder.itemView.setOnClickListener {
//            inspectionDataBrowserActivity.startHiveInspectionDataActivity(currentHiveInspectionData.id)
//        }
//        holder.itemView.setOnLongClickListener() {
//            showContextMenu(holder.itemView, currentHiveInspectionData)
//            true
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return inspectionDataList.size
//    }
//
//    inner class HiveInspectionDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val tvHiveName = itemView.findViewById<TextView>(R.id.tv_hive_name)
//        private val tvFreeFrames = itemView.findViewById<TextView>(R.id.tv_free_frames)
//        private val tvInspectionNotes = itemView.findViewById<TextView>(R.id.tv_inspection_notes)
//
//        fun bind(inspectionData: HiveInspectionData) {
//            val hiveName = HivesFunctionality.getHiveNameById(db, inspectionData.hiveId )
//            tvHiveName.text = hiveName
//            tvFreeFrames.text = inspectionData.freeSpaceFrames.toString()
//            tvInspectionNotes.text = inspectionData.notes
//        }
//    }
//
//
//    private fun showContextMenu(view: View, inspectionData: HiveInspectionData) {
//        val popupMenu = PopupMenu(view.context, view)
//        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_long_click_browser, popupMenu.menu)
//
//        popupMenu.setOnMenuItemClickListener { item ->
//            when (item.itemId) {
//                R.id.menu_long_click_delete -> {
//                    Utils.showConfirmationDialog(
//                        view.context,
//                        "Are you sure you want to delete this inspection data?"
//                    ) { confirmed ->
//                        if (confirmed) {
//                            // User confirmed the deletion
//                            InspectionsFunctionality.deleteHiveInspectionData(db, inspectionData.id)
//                            val (HiveInspectionData, result) = InspectionsFunctionality.getAllHiveInspectionData(db, inspectionId)
//                            if (result == 1) {
//                                updateData(HiveInspectionData)
//                            }
//                        }
//                    }
//                    true
//                }
//                R.id.menu_long_click_adjust -> {
//                    // Handle adjust action
//                    inspectionDataBrowserActivity.startAdjustHiveInspectionDataActivity()
//                    true
//                }
//                else -> false
//            }
//        }
//
//        popupMenu.show()
//    }
//}
