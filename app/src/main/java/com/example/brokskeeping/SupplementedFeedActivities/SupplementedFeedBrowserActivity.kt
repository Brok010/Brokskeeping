package com.example.brokskeeping.SupplementedFeedActivities

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
import com.example.brokskeeping.DbFunctionality.HoneyHarvestFunctionality
import com.example.brokskeeping.DbFunctionality.SupplementedFeedFunctionality
import com.example.brokskeeping.HoneyHarvestActivities.HoneyHarvestAdapter
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding
import java.util.Calendar

class SupplementedFeedBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var supplementedFeedAdapter: SupplementedFeedAdapter
    private var selectedYear: Int? = null
    private var selectedMonth: Int? = null
    private var selectedType: String = "Station"
    private lateinit var header: TextView
    private lateinit var timeFilterInput: EditText
    private lateinit var typeFilterInput: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        supplementedFeedAdapter = SupplementedFeedAdapter(mutableListOf(), "Station", db, this)

        header = binding.tvCommonBrowserHeader
        header.text = "Supplemented feed"
        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SupplementedFeedBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = supplementedFeedAdapter
        }

        createAndAddFilterLayout()
    }

    override fun onResume() {
        super.onResume()
        val (filteredList, result) = SupplementedFeedFunctionality.getFilteredSupplementedFeed(db, selectedYear, selectedMonth, selectedType)

        if (result == 1) {
            supplementedFeedAdapter.updateData(filteredList, selectedType)
        } else {
            Toast.makeText(this, "Wrong filter selection or no data found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAndAddFilterLayout() {
        val filterLayout = binding.llFilters
        filterLayout.removeAllViews()

        val timeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 16
            }
        }

        val timeLabel = TextView(this).apply {
            text = "Time"
            setTextColor(ContextCompat.getColor(this@SupplementedFeedBrowserActivity, com.example.brokskeeping.R.color.basicTextColor))
        }

        timeFilterInput = EditText(this).apply {
            id = View.generateViewId()
            hint = "Time Filter"
            isFocusable = false
            isClickable = true
            setText("All Time")
            inputType = android.text.InputType.TYPE_NULL
            setPadding(16, 0, 16, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 8
                marginEnd = 8
            }
        }

        timeLayout.addView(timeLabel)
        timeLayout.addView(timeFilterInput)

        val typeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val typeLabel = TextView(this).apply {
            text = "Type"
            setTextColor(ContextCompat.getColor(this@SupplementedFeedBrowserActivity, com.example.brokskeeping.R.color.basicTextColor))
        }

        typeFilterInput = EditText(this).apply {
            id = View.generateViewId()
            hint = "Type Filter"
            isFocusable = false
            isClickable = true
            setText("Station")
            inputType = android.text.InputType.TYPE_NULL
            setPadding(16, 0, 16, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 8
                marginEnd = 8
            }
        }

        typeLayout.addView(typeLabel)
        typeLayout.addView(typeFilterInput)

        filterLayout.addView(timeLayout)
        filterLayout.addView(typeLayout)


        // Optional: Set listeners
        timeFilterInput.setOnClickListener { showTimeFilterDialog() }
        typeFilterInput.setOnClickListener { showTypeFilterDialog() }

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
                    timeFilterInput.setText("All Time")
                    selectedMonth = null
                    selectedYear = null
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
                timeFilterInput.setText(selectedYear.toString())
                selectedMonth = null
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
                timeFilterInput.setText("${selectedMonth.toString()}/${selectedYear.toString()}")
                onResume()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTypeFilterDialog() {
        val options = arrayOf("Hive", "Station")
        AlertDialog.Builder(this)
            .setTitle("Select Type Filter")
            .setItems(options) { _, which ->
                typeFilterInput.setText(options[which])
                selectedType = options[which]
                onResume()
            }
            .show()
    }
}