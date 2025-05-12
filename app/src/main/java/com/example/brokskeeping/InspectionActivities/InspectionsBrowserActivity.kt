package com.example.brokskeeping.InspectionActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.InspectionDataActivities.InspectionDataBrowserActivity
import com.example.brokskeeping.databinding.ActivityInspectionsBrowserBinding

class InspectionsBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInspectionsBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var inspectionsAdapter: InspectionsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInspectionsBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        inspectionsAdapter = InspectionsAdapter(mutableListOf(), db, this)

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InspectionsBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = inspectionsAdapter
        }

        binding.NewInspectionBt.setOnClickListener {
            startNewInspectionActivity()
        }
    }
    
    override fun onResume() {
        super.onResume()
        val updatedInspectionsList = InspectionsFunctionality.getAllInspections(db)
        inspectionsAdapter.updateData(updatedInspectionsList)
    }

    fun startNewInspectionActivity() {
        val stations = StationsFunctionality.getAllStations(db)
        val stationNames = stations.map { it.name }

        // Show a dialog to let the user choose a station
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select a Station")

        builder.setItems(stationNames.toTypedArray()) { dialog, which ->
            val chosenName = stationNames[which]
            val chosenStationId = StationsFunctionality.getStationIdByName(db, chosenName)

            val intent = Intent(this, InspectionActivity::class.java)
            intent.putExtra("stationId", chosenStationId)
            startActivity(intent)
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
    fun startInspectionDataBrowserActivity(inspectionId: Int) {
        val intent = Intent(this, InspectionDataBrowserActivity::class.java)
        intent.putExtra("inspectionId", inspectionId)
        startActivity(intent)
    }

    fun startAdjustInspectionActivity() {
        //
    }
}