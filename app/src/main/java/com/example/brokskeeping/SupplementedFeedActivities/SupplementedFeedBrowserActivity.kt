package com.example.brokskeeping.SupplementedFeedActivities

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
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding
import java.util.Calendar

class SupplementedFeedBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var supplementedFeedAdapter: SupplementedFeedAdapter
    private var selectedYear: Int? = null
    private var selectedMonth: Int? = null
    private var selectedType: String = ""
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
        selectedType = getString(R.string.station)

        header = binding.tvCommonBrowserHeader
        header.text = getString(R.string.supplemented_feed)
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
        val (filteredList, result) = SupplementedFeedFunctionality.getFilteredSupplementedFeed(this, db, selectedYear, selectedMonth, selectedType)

        if (result == 1) {
            supplementedFeedAdapter.updateData(filteredList, selectedType)
        } else {
            Toast.makeText(this, getString(R.string.wrong_filter_selection_or_no_data_found), Toast.LENGTH_SHORT).show()
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
            text = getString(R.string.time)
            setTextColor(ContextCompat.getColor(this@SupplementedFeedBrowserActivity, com.example.brokskeeping.R.color.basicTextColor))
        }

        timeFilterInput = EditText(this).apply {
            id = View.generateViewId()
            hint = getString(R.string.time_filter)
            isFocusable = false
            isClickable = true
            setText(getString(R.string.all_time))
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
            text = getString(R.string.type)
            setTextColor(ContextCompat.getColor(this@SupplementedFeedBrowserActivity, com.example.brokskeeping.R.color.basicTextColor))
        }

        typeFilterInput = EditText(this).apply {
            id = View.generateViewId()
            hint = getString(R.string.type_filter)
            isFocusable = false
            isClickable = true
            setText(getString(R.string.station))
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
                    timeFilterInput.setText(getString(R.string.all_time))
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
        yearPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        layout.addView(TextView(this).apply { text = getString(R.string.select_year) })
        layout.addView(yearPicker)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_year))
            .setView(layout)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                selectedYear = yearPicker.selectedItem.toString().toIntOrNull()
                timeFilterInput.setText(selectedYear.toString())
                selectedMonth = null
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
                timeFilterInput.setText("${selectedMonth.toString()}/${selectedYear.toString()}")
                onResume()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showTypeFilterDialog() {
        val options = arrayOf(getString(R.string.hive), getString(R.string.station))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_type_filter))
            .setItems(options) { _, which ->
                typeFilterInput.setText(options[which])
                selectedType = options[which]
                onResume()
            }
            .show()
    }
}