package com.example.brokskeeping.HoneyHarvestActivities

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper

import com.example.brokskeeping.DbFunctionality.HoneyHarvestFunctionality
import com.example.brokskeeping.databinding.ActivityHoneyHarvestBrowserBinding
import java.util.Calendar

class HoneyHarvestBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHoneyHarvestBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var honeyHarvestAdapter: HoneyHarvestAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHoneyHarvestBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        honeyHarvestAdapter = HoneyHarvestAdapter(mutableListOf(), "Station", db, this)

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HoneyHarvestBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = honeyHarvestAdapter
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        binding.timeFilterInput.setText("0/$currentYear")
        binding.typeFilterInput.setText("Station")

        binding.timeFilterInput.setOnClickListener {
            showTimeFilterDialog()
        }

        binding.typeFilterInput.setOnClickListener {
            showTypeFilterDialog()
        }

        binding.resetFiltersButton.setOnClickListener {
            applyFilters()
        }

    }

    override fun onResume() {
        super.onResume()
        applyFilters()
    }


    private fun applyFilters() {
        val timeText = binding.timeFilterInput.text.toString()
        val typeText = binding.typeFilterInput.text.toString()

        val (month, year) = when {
            timeText.equals("All Time", ignoreCase = true) -> 0 to 0
            timeText.contains("/") -> {
                val parts = timeText.split("/")
                val m = parts[0].toIntOrNull() ?: 0
                val y = parts.getOrNull(1)?.toIntOrNull() ?: 0
                m to y
            }
            else -> 0 to 0
        }

        val typeFilter = typeText.ifBlank { "Station" }
        val (filteredList, result) = HoneyHarvestFunctionality.getFilteredHoneyHarvests(db, year, month, typeFilter)

        if (result == 1) {
            honeyHarvestAdapter.updateData(filteredList, typeFilter)
        } else {
            Toast.makeText(this, "Wrong filter selection or no data found", Toast.LENGTH_SHORT).show()
        }
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
                    binding.timeFilterInput.setText("All Time")
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
        layout.addView(TextView(this).apply { text = "Select Year" })
        layout.addView(yearPicker)

        AlertDialog.Builder(this)
            .setTitle("Choose Year")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = yearPicker.selectedItem.toString()
                binding.timeFilterInput.setText("0/$selectedYear")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMonthYearPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 10..currentYear + 1).map { it.toString() }.toTypedArray()
        val months = (1..12).map { it.toString().padStart(2, '0') }.toTypedArray()

        var selectedYear = years[0]
        var selectedMonth = months[0]

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val yearPicker = Spinner(this)
        yearPicker.adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, years)
        layout.addView(TextView(this).apply { text = "Select Year" })
        layout.addView(yearPicker)

        val monthPicker = Spinner(this)
        monthPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        layout.addView(TextView(this).apply { text = "Select Month" })
        layout.addView(monthPicker)

        AlertDialog.Builder(this)
            .setTitle("Choose Month and Year")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                selectedYear = yearPicker.selectedItem.toString()
                selectedMonth = monthPicker.selectedItem.toString()
                binding.timeFilterInput.setText("$selectedMonth/$selectedYear")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTypeFilterDialog() {
        val options = arrayOf("Hive", "Station")
        AlertDialog.Builder(this)
            .setTitle("Select Type Filter")
            .setItems(options) { _, which ->
                binding.typeFilterInput.setText(options[which])
            }
            .show()
    }




}

