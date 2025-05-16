package com.example.brokskeeping.InspectionActivities

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.InspectionDataActivities.InspectionDataBrowserActivity
import com.example.brokskeeping.databinding.ActivityInspectionsBrowserBinding
import java.util.Calendar

class InspectionsBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInspectionsBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var inspectionsAdapter: InspectionsAdapter
    private var filterStationId: Int? = null
    private var selectedYear: Int? = null
    private var selectedMonth: Int? = null
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

        binding.stationFilterInput.setOnClickListener {
            stationFilter()
        }

        binding.dateFilterInput.setOnClickListener {
            showTimeFilterDialog()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (filterStationId == null || filterStationId == 0) {
            binding.stationFilterInput.setText("All")
        }
        if (selectedYear == null || selectedYear == 0) {
            binding.dateFilterInput.setText("All Time")
        }
        val (updatedInspectionsList, result) = InspectionsFunctionality.getAllInspections(db, filterStationId, selectedYear, selectedMonth)
        if (result != 1) {
            Toast.makeText(this, "Could not load inspections", Toast.LENGTH_SHORT).show()
            finish()
        }
        inspectionsAdapter.updateData(updatedInspectionsList)
    }

    private fun showTimeFilterDialog() {
        val options = arrayOf("Month", "Year", "All Time")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Time Filter")
        builder.setItems(options) { _, which ->
            when (options[which]) {
                "Month" -> {
                    showMonthYearPicker()
                }
                "Year" -> {
                    showYearPicker()
                }
                "All Time" -> {
                    binding.dateFilterInput.setText("All Time")
                    selectedYear = null
                    selectedMonth = null
                    onResume()
                }
            }
        }
        builder.show()
    }

    private fun showYearPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 10..currentYear + 1).map { it.toString() }.toTypedArray()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val yearPicker = Spinner(this)
        yearPicker.adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, years)
        layout.addView(TextView(this).apply { text = "Select Year" })
        layout.addView(yearPicker)

        AlertDialog.Builder(this)
            .setTitle("Choose Year")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                selectedYear = yearPicker.selectedItem.toString().toIntOrNull()
                selectedMonth = null
                binding.dateFilterInput.setText("${selectedYear.toString()}")
                onResume()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMonthYearPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 10..currentYear + 1).map { it.toString() }.toTypedArray()
        val months = (1..12).map { it.toString().padStart(2, '0') }.toTypedArray()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val yearPicker = Spinner(this)
        yearPicker.adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, years)
        layout.addView(TextView(this).apply { text = "Select Year" })
        layout.addView(yearPicker)

        val monthPicker = Spinner(this)
        monthPicker.adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, months)
        layout.addView(TextView(this).apply { text = "Select Month" })
        layout.addView(monthPicker)

        AlertDialog.Builder(this)
            .setTitle("Choose Month and Year")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                selectedYear = yearPicker.selectedItem.toString().toIntOrNull()
                selectedMonth = monthPicker.selectedItem.toString().toIntOrNull()
                binding.dateFilterInput.setText("${selectedMonth.toString()}/${selectedYear.toString()}")
                onResume()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun stationFilter() {
        val (stations, result) = StationsFunctionality.getAllStations(db, 1)
        if (result == 0) {
            Log.e("InspectionBrowserActivity", "Station loading was not successful - stationFilter")
        }

        val stationNames = mutableListOf("All")
        val stationIds = mutableListOf(0)

        stations.forEach {
            stationNames.add(StationsFunctionality.getStationNameById(db, it.id))
            stationIds.add(it.id)
        }

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Choose a station")
        builder.setItems(stationNames.toTypedArray()) { _, which ->
            filterStationId = if (which == 0) null else stationIds[which]
            binding.stationFilterInput.setText(stationNames[which])
            onResume()
        }
        builder.show()
    }

    private fun startNewInspectionActivity() {
        val (stations, result) = StationsFunctionality.getAllStations(db, 1)
        if (result == 0) {
            Toast.makeText(this, "Station loading was not successful", Toast.LENGTH_SHORT).show()
            finish()
        }

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
    fun startInspectionDataBrowserActivity(inspectionId: Int, stationId: Int) {
        val intent = Intent(this, InspectionDataBrowserActivity::class.java)
        intent.putExtra("inspectionId", inspectionId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }

    fun startAdjustInspectionActivity() {
        //
    }
}