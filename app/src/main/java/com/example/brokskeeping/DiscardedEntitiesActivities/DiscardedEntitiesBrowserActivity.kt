
package com.example.brokskeeping.DiscardedEntitiesActivities

import android.content.Intent
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
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DataClasses.DiscardedEntity
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.HiveActivities.HiveActivity
import com.example.brokskeeping.InspectionDataActivities.InspectionDataBrowserActivity
import com.example.brokskeeping.R
import com.example.brokskeeping.StationActivities.StationActivity
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding
import java.util.Calendar

class DiscardedEntitiesBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var discardedEntitiesAdapter: DiscardedEntitiesAdapter
    private var selectedCreationYear: Int? = null
    private var selectedCreationMonth: Int? = null
    private var selectedDeathYear: Int? = null
    private var selectedDeathMonth: Int? = null
    private var selectedType: String = "All"
    private lateinit var header: TextView
    private lateinit var creationTimeFilterInput: EditText
    private lateinit var deathTimeFilterInput: EditText
    private lateinit var typeFilterInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        discardedEntitiesAdapter = DiscardedEntitiesAdapter(mutableListOf(), db, this)

        header = binding.tvCommonBrowserHeader
        header.text = "Discarded Entities"
        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscardedEntitiesBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = discardedEntitiesAdapter
        }
        createAndAddFilterLayout()
    }

    override fun onResume() {
        super.onResume()

        val discardedEntities = mutableListOf<DiscardedEntity>()

        when (selectedType) {
            "Station" -> {
                val (resultStations, result) = StationsFunctionality.getAllStations(
                    db, 0, selectedCreationYear, selectedCreationMonth, selectedDeathYear, selectedDeathMonth
                )
                if (result == 0) {
                    Toast.makeText(this, "Stations couldn't be found", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    discardedEntities.addAll(resultStations.map { DiscardedEntity.DiscardedStation(it) })
                }
            }

            "Hive" -> {
                val (resultHives, result) = HivesFunctionality.getAllHives(
                    db, null, 1, selectedCreationYear, selectedCreationMonth, selectedDeathYear, selectedDeathMonth
                )
                if (result == 0) {
                    Toast.makeText(this, "Hives couldn't be found", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    discardedEntities.addAll(resultHives.map { DiscardedEntity.DiscardedHive(it) })
                }
            }

            else -> {
                val (resultStations, stationsResult) = StationsFunctionality.getAllStations(
                    db, 0, selectedCreationYear, selectedCreationMonth, selectedDeathYear, selectedDeathMonth
                )
                val (resultHives, hivesResult) = HivesFunctionality.getAllHives(
                    db, null, 1, selectedCreationYear, selectedCreationMonth, selectedDeathYear, selectedDeathMonth
                )

                if (stationsResult == 0 || hivesResult == 0) {
                    Toast.makeText(this, "Entities couldn't be found", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    discardedEntities.addAll(resultStations.map { DiscardedEntity.DiscardedStation(it) })
                    discardedEntities.addAll(resultHives.map { DiscardedEntity.DiscardedHive(it) })
                }
            }
        }

        discardedEntitiesAdapter.updateData(discardedEntities)
    }


    private fun createAndAddFilterLayout() {
        val filterLayout = binding.llFilters
        filterLayout.removeAllViews()

        // Colors
        val textColor = ContextCompat.getColor(this, R.color.basicTextColor)

        // Helper function to create filter item
        fun createFilter(labelText: String, startText: String, editTextId: Int): LinearLayout {
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = 16
                }
            }

            val label = TextView(this).apply {
                text = labelText
                setTextColor(textColor)
            }

            val input = EditText(this).apply {
                id = editTextId
                setText(startText)
                isFocusable = false
                isClickable = true
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

            layout.addView(label)
            layout.addView(input)
            return layout
        }

        // Create individual filter views
        val typeLayout = createFilter("Type", "All", View.generateViewId())
        val creationLayout = createFilter("Creation", "All Time", View.generateViewId())
        val deathLayout = createFilter("Death", "All Time", View.generateViewId())

        // Save references if needed
        typeFilterInput = typeLayout.getChildAt(1) as EditText
        creationTimeFilterInput = creationLayout.getChildAt(1) as EditText
        deathTimeFilterInput = deathLayout.getChildAt(1) as EditText

        // Add to container
        filterLayout.addView(typeLayout)
        filterLayout.addView(creationLayout)
        filterLayout.addView(deathLayout)

        // Set listeners
        typeFilterInput.setOnClickListener { showTypeFilterDialog() }
        creationTimeFilterInput.setOnClickListener { showTimeFilterDialog(1) }
        deathTimeFilterInput.setOnClickListener { showTimeFilterDialog(0) }
    }


    private fun showTimeFilterDialog(creation: Int) {
        val options = arrayOf("Month", "Year", "All Time")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Time Filter")
        builder.setItems(options) { _, which ->
            when (options[which]) {
                "Month" -> {
                    showMonthYearPicker(creation)
                }
                "Year" -> {
                    showYearPicker(creation)
                }
                "All Time" -> {
                    if (creation == 1) {
                        creationTimeFilterInput.setText("All Time")
                        selectedCreationMonth = null
                        selectedCreationYear = null
                    } else {
                        deathTimeFilterInput.setText("All Time")
                        selectedDeathMonth = null
                        selectedDeathYear = null
                    }
                    onResume()
                }
            }
        }
        builder.show()
    }

    private fun showYearPicker(creation: Int) {
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
                if (creation == 1) {
                    selectedCreationMonth = null
                    selectedCreationYear = yearPicker.selectedItem.toString().toIntOrNull()
                    creationTimeFilterInput.setText(selectedCreationYear.toString())
                } else {
                    selectedDeathMonth = null
                    selectedDeathYear = yearPicker.selectedItem.toString().toIntOrNull()
                    deathTimeFilterInput.setText(selectedDeathYear.toString())
                }
                onResume()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMonthYearPicker(creation: Int) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 10..currentYear + 1).map { it.toString() }.toTypedArray()
        val months = (1..12).map { it.toString().padStart(2, '0') }.toTypedArray()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val yearPicker = Spinner(this)
        yearPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
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
                if (creation == 1) {
                    selectedCreationMonth = monthPicker.selectedItem.toString().toIntOrNull()
                    selectedCreationYear = yearPicker.selectedItem.toString().toIntOrNull()
                    creationTimeFilterInput.setText(selectedCreationYear.toString())
                } else {
                    selectedDeathMonth = monthPicker.selectedItem.toString().toIntOrNull()
                    selectedDeathYear = yearPicker.selectedItem.toString().toIntOrNull()
                    deathTimeFilterInput.setText(selectedDeathYear.toString())
                }
                onResume()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTypeFilterDialog() {
        val options = arrayOf("All", "Hive", "Station")
        AlertDialog.Builder(this)
            .setTitle("Select Type Filter")
            .setItems(options) { _, which ->
                typeFilterInput.setText(options[which])
                selectedType = options[which]
                onResume()
            }
            .show()
    }

    fun startHiveActivity(hiveId: Int) {
        val intent = Intent(this, HiveActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }

    fun startStationActivity(stationId: Int) {
        val intent = Intent(this, StationActivity::class.java)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
}

