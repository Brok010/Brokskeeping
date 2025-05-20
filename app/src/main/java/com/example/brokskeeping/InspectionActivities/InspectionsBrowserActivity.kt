package com.example.brokskeeping.InspectionActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.InspectionDataActivities.InspectionDataBrowserActivity
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding
import java.util.Calendar
import com.example.brokskeeping.R

class InspectionsBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var inspectionsAdapter: InspectionsAdapter
    private var filterStationId: Int? = null
    private var selectedYear: Int? = null
    private var selectedMonth: Int? = null
    private lateinit var header: TextView
    private lateinit var btnLayout: LinearLayout
    private lateinit var stationFilterInput: EditText
    private lateinit var dateFilterInput: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        inspectionsAdapter = InspectionsAdapter(mutableListOf(), db, this)
        header = binding.tvCommonBrowserHeader
        header.text = getString(R.string.inspections)

        // buttons
        btnLayout = binding.llCommonBrowserButtonLayout
        val newInspectionButton = Button(this).apply {
            id = View.generateViewId()
            text = getString(R.string.new_inspection)
            setTextColor(ContextCompat.getColor(this@InspectionsBrowserActivity, com.example.brokskeeping.R.color.buttonTextColor))
            backgroundTintList = ContextCompat.getColorStateList(this@InspectionsBrowserActivity, com.example.brokskeeping.R.color.buttonColor)
        }
        btnLayout.addView(newInspectionButton)

        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@InspectionsBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = inspectionsAdapter
        }

        newInspectionButton.setOnClickListener {
            startNewInspectionActivity()
        }

        createAndAddFilterLayout()

        stationFilterInput.setOnClickListener {
            stationFilter()
        }

        dateFilterInput.setOnClickListener {
            showTimeFilterDialog()
        }
    }

    private fun createAndAddFilterLayout() {
        val filterLayout = binding.llFilters
        filterLayout.removeAllViews()

        // Station filter layout
        val stationLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 8
            }
        }

        val stationLabel = TextView(this).apply {
            text = getString(R.string.station)
            setTextColor(ContextCompat.getColor(this@InspectionsBrowserActivity, com.example.brokskeeping.R.color.basicTextColor))
        }

        stationFilterInput = EditText(this).apply {
            id = View.generateViewId()
            hint = context.getString(R.string.station_filter)
            isFocusable = false
            isClickable = true
            inputType = InputType.TYPE_NULL
            setPadding(16, 0, 16, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 8
                marginEnd = 8
            }
        }

        stationLayout.addView(stationLabel)
        stationLayout.addView(stationFilterInput)

        // Date filter layout
        val dateLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 16
            }
        }

        val dateLabel = TextView(this).apply {
            text = getString(R.string.date)
            setTextColor(ContextCompat.getColor(this@InspectionsBrowserActivity, com.example.brokskeeping.R.color.basicTextColor))
        }

        dateFilterInput = EditText(this).apply {
            id = View.generateViewId()
            hint = context.getString(R.string.date_filter)
            isFocusable = false
            isClickable = true
            inputType = InputType.TYPE_NULL
            setPadding(16, 0, 16, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 8
                marginEnd = 8
            }
        }

        dateLayout.addView(dateLabel)
        dateLayout.addView(dateFilterInput)

        // Add both to root filter layout
        filterLayout.addView(stationLayout)
        filterLayout.addView(dateLayout)

        stationFilterInput.setOnClickListener {
            stationFilter()
        }

        dateFilterInput.setOnClickListener {
            showTimeFilterDialog()
        }

    }
    
    override fun onResume() {
        super.onResume()
        if (filterStationId == null || filterStationId == 0) {
            stationFilterInput.setText(getString(R.string.all))
        }
        if (selectedYear == null || selectedYear == 0) {
            dateFilterInput.setText(getString(R.string.all_time))
        }
        val (updatedInspectionsList, result) = InspectionsFunctionality.getAllInspections(db, filterStationId, selectedYear, selectedMonth)
        if (result != 1) {
            Toast.makeText(this, getString(R.string.could_not_load_inspections), Toast.LENGTH_SHORT).show()
            finish()
        }
        inspectionsAdapter.updateData(updatedInspectionsList)
    }

    private fun showTimeFilterDialog() {
        val options = arrayOf(getString(R.string.month), getString(R.string.year), getString(R.string.all_time))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_time_filter))
        builder.setItems(options) { _, which ->
            when (options[which]) {
                getString(R.string.month) -> {
                    showMonthYearPicker()
                }
                getString(R.string.year) -> {
                    showYearPicker()
                }
                getString(R.string.all_time) -> {
                    dateFilterInput.setText(getString(R.string.all_time))
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
        yearPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        layout.addView(TextView(this).apply { text = getString(R.string.select_year) })
        layout.addView(yearPicker)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_year))
            .setView(layout)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                selectedYear = yearPicker.selectedItem.toString().toIntOrNull()
                selectedMonth = null
                dateFilterInput.setText("${selectedYear.toString()}")
                onResume()
            }
            .setNegativeButton(getString(R.string.cancel), null)
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
        yearPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        layout.addView(TextView(this).apply { text = getString(R.string.select_year) })
        layout.addView(yearPicker)

        val monthPicker = Spinner(this)
        monthPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        layout.addView(TextView(this).apply { text = getString(R.string.select_month) })
        layout.addView(monthPicker)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_month_and_year))
            .setView(layout)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                selectedYear = yearPicker.selectedItem.toString().toIntOrNull()
                selectedMonth = monthPicker.selectedItem.toString().toIntOrNull()
                dateFilterInput.setText("${selectedMonth.toString()}/${selectedYear.toString()}")
                onResume()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    private fun stationFilter() {
        val (stations, result) = StationsFunctionality.getAllStations(db, 1)
        if (result == 0) {
            Log.e("InspectionBrowserActivity", "Station loading was not successful - stationFilter")
        }

        val stationNames = mutableListOf(getString(R.string.all))
        val stationIds = mutableListOf(0)

        stations.forEach {
            stationNames.add(StationsFunctionality.getStationNameById(db, it.id))
            stationIds.add(it.id)
        }

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_a_station))
        builder.setItems(stationNames.toTypedArray()) { _, which ->
            filterStationId = if (which == 0) null else stationIds[which]
            stationFilterInput.setText(stationNames[which])
            onResume()
        }
        builder.show()
    }

    private fun startNewInspectionActivity() {
        val (stations, result) = StationsFunctionality.getAllStations(db, 1)
        if (result == 0) {
            Toast.makeText(this, getString(R.string.station_loading_was_not_successful), Toast.LENGTH_SHORT).show()
            finish()
        }

        val stationNames = stations.map { it.name }

        // Show a dialog to let the user choose a station
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_a_station))

        builder.setItems(stationNames.toTypedArray()) { dialog, which ->
            val chosenName = stationNames[which]
            val chosenStationId = StationsFunctionality.getStationIdByName(db, chosenName)

            val intent = Intent(this, InspectionActivity::class.java)
            intent.putExtra("stationId", chosenStationId)
            startActivity(intent)
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
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