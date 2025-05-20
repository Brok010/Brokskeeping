package com.example.brokskeeping.InspectionDataActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
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
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding
import java.util.Calendar

class HiveInspectionDataBrowserActivity : AppCompatActivity(), HiveInspectionDataAdapter.OnDataChangedListener {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var hiveInspectionDataAdapter: HiveInspectionDataAdapter
    private var hiveId: Int? = null
    private var selectedMonth: Int? = null
    private var selectedYear: Int? = null
    private lateinit var header: TextView
    private lateinit var dateFilterInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        hiveId = intent.getIntExtra("hiveId", -1)
        hiveInspectionDataAdapter = HiveInspectionDataAdapter(mutableListOf(), hiveId!!, db, this, this)
        val hiveName = HivesFunctionality.getHiveNameById(db, hiveId!!)

        // Bind views using ViewBinding
        header = binding.tvCommonBrowserHeader
        header.text = getString(R.string.inspections_of_hive, hiveName)

        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HiveInspectionDataBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = hiveInspectionDataAdapter
        }

        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(binding.commonFragmentContainer.id, fragment)
                .commit()
        }

        createAndAddFilterLayout()

    }
    override fun onDataChanged() {
        // This will refresh data correctly
        onResume()
    }


    override fun onResume() {
        super.onResume()
        if (selectedYear == null || selectedYear == 0) {
            dateFilterInput.setText(getString(R.string.all_time))
        }
        val (updatedInspectionDataList, result) = InspectionsFunctionality.getAllInspectionDataForHiveId(db, hiveId!!, selectedYear, selectedMonth, true)
        if (result == 1) {
            hiveInspectionDataAdapter.updateData(updatedInspectionDataList)
        } else {
            Toast.makeText(this, getString(R.string.cannot_load_inspection_data), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun createAndAddFilterLayout() {
        val filterLayout = binding.llFilters
        filterLayout.removeAllViews()

        // Date filter layout
        val dateLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 16
            }
        }

        val dateLabel = TextView(this).apply {
            text = getString(R.string.date)
            setTextColor(ContextCompat.getColor(this@HiveInspectionDataBrowserActivity, com.example.brokskeeping.R.color.basicTextColor))
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
        filterLayout.addView(dateLayout)

        dateFilterInput.setOnClickListener {
            showTimeFilterDialog()
        }
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

    fun startInspectionDataActivity(inspectionDataId: Int) {
        val intent = Intent(this, InspectionDataActivity::class.java)
        intent.putExtra("inspectionDataId", inspectionDataId)
        startActivity(intent)
    }

    fun startAdjustInspectionDataActivity() {
        //
    }
}