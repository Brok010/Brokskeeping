package com.example.brokskeeping.HiveActivities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R

class HivesAdapter(private val hivesList: MutableList<Beehive>,
                   private val stationId: Int,
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
            hivesBrowserActivity.startHiveActivity(currentHive.id)
            true
        }
        holder.itemView.setOnLongClickListener {
            showContextMenu(holder.itemView, currentHive)
            true
        }
    }

    override fun getItemCount(): Int {
        return hivesList.size
    }

    inner class HiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStationName: TextView = itemView.findViewById(R.id.tv_value_hives_name)
        private val rlHiveBrowser: RelativeLayout = itemView.findViewById(R.id.rl_outer_hive_browser)

        fun bind(hive: Beehive) {
            val context = itemView.context
            tvStationName.text = hive.nameTag
            val colorResId = when (hive.attentionWorth) {
                1 -> R.color.itemColorAttention1
                2 -> R.color.itemColorAttention2
                3 -> R.color.itemColorAttention3
                4 -> R.color.itemColorAttention4
                5 -> R.color.itemColorAttention5
                else -> R.color.itemColor
            }
            rlHiveBrowser.setBackgroundColor(context.getColor(colorResId))
        }
    }

    private fun showContextMenu(view: View, hive: Beehive) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_long_click_hive_browser, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_long_click_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        view.context.getString(R.string.are_you_sure_you_want_to_delete_this_hive)
                    ) { confirmed ->
                        if (confirmed) {
                            // User confirmed the deletion
                            HivesFunctionality.deleteHive(db, stationId, hive.id)
                            val (hiveIds, result) = HivesFunctionality.getAllHives(db, stationId, 0)
                            if (result == 1) {
                                updateData(hiveIds)
                            } else {
                                Log.e("HiveAdapter", "getAllHives function in did not finish properly")
                            }

                        }
                    }
                    true
                }
                R.id.menu_long_click_adjust -> {
                    hivesBrowserActivity.startAdjustHiveActivity(stationId, hive.id)
                    true
                }
                R.id.menu_long_click_inspections -> {
                    hivesBrowserActivity.startHiveInspectionDataBrowserActivity(hive.id)
                    true
                }
                R.id.menu_long_click_notes -> {
                    hivesBrowserActivity.startNotesBrowserActivity(stationId, hive.id)
                    true
                }
                R.id.menu_long_click_todos -> {
                    hivesBrowserActivity.startToDoBrowserActivity(hive.id)
                    true
                }
                R.id.menu_long_click_logs -> {
                    hivesBrowserActivity.startLogsBrowserActivity(hive.id)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }
}
