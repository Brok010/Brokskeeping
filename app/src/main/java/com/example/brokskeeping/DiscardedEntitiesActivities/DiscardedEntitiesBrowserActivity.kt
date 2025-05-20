
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
import com.example.brokskeeping.DataClasses.DiscardedEntity
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.HiveActivities.HiveActivity
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
    private var selectedType: String = ""
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
        selectedType = getString(R.string.all)
        discardedEntitiesAdapter = DiscardedEntitiesAdapter(mutableListOf(), db, this)

        header = binding.tvCommonBrowserHeader
        header.text = getString(R.string.discarded_entities)
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
            getString(R.string.station) -> {
                val (resultStations, result) = StationsFunctionality.getAllStations(
                    db, 0, selectedCreationYear, selectedCreationMonth, selectedDeathYear, selectedDeathMonth
                )
                if (result == 0) {
                    Toast.makeText(this,
                        getString(R.string.stations_couldn_t_be_found), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    discardedEntities.addAll(resultStations.map { DiscardedEntity.DiscardedStation(it) })
                }
            }

            getString(R.string.hive) -> {
                val (resultHives, result) = HivesFunctionality.getAllHives(
                    db, null, 1, selectedCreationYear, selectedCreationMonth, selectedDeathYear, selectedDeathMonth
                )
                if (result == 0) {
                    Toast.makeText(this,
                        getString(R.string.hives_couldn_t_be_found), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this,
                        getString(R.string.entities_couldn_t_be_found), Toast.LENGTH_SHORT).show()
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
        val typeLayout = createFilter(getString(R.string.type), getString(R.string.all), View.generateViewId())
        val creationLayout = createFilter(getString(R.string.creation), getString(R.string.all_time), View.generateViewId())
        val deathLayout = createFilter(getString(R.string.death), getString(R.string.all_time), View.generateViewId())

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
        val options = arrayOf(getString(R.string.month), getString(R.string.year), getString(R.string.all_time))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_time_filter))
        builder.setItems(options) { _, which ->
            when (options[which]) {
                getString(R.string.month) -> {
                    showMonthYearPicker(creation)
                }
                getString(R.string.year) -> {
                    showYearPicker(creation)
                }
                getString(R.string.all_time) -> {
                    if (creation == 1) {
                        creationTimeFilterInput.setText(getString(R.string.all_time))
                        selectedCreationMonth = null
                        selectedCreationYear = null
                    } else {
                        deathTimeFilterInput.setText(getString(R.string.all_time))
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
        layout.addView(TextView(this).apply { text = context.getString(R.string.select_year) })
        layout.addView(yearPicker)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_year))
            .setView(layout)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
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
            .setNegativeButton(getString(R.string.cancel), null)
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
        layout.addView(TextView(this).apply { text = getString(R.string.select_year) })
        layout.addView(yearPicker)

        val monthPicker = Spinner(this)
        monthPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        layout.addView(TextView(this).apply { text = context.getString(R.string.select_month) })
        layout.addView(monthPicker)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_month_and_year))
            .setView(layout)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
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
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showTypeFilterDialog() {
        val options = arrayOf(getString(R.string.all), getString(R.string.hive), getString(R.string.station))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_type_filter))
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

