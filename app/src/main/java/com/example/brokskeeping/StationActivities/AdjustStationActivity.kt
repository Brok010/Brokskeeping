package com.example.brokskeeping.StationActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R
import java.util.Date

class AdjustStationActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private var stationId: Int = -1
    private lateinit var etName: EditText
    private lateinit var etLocation: EditText
    private lateinit var etHiveCount: EditText
    private lateinit var checkboxInUse: CheckBox
    private var beehiveNum: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_station)

        db = DatabaseHelper(this)

        // Retrieve stationId from the intent
        stationId = intent.getIntExtra("stationId", -1)

        etName = findViewById(R.id.et_adjust_station_name)
        etLocation = findViewById(R.id.et_adjust_station_location)
        etHiveCount = findViewById(R.id.et_adjust_station_hive_count)
        checkboxInUse = findViewById(R.id.checkbox_in_use)

        // Load existing station data
        var (existingStation, stationResult) = StationsFunctionality.getStationsAttributes(db, stationId)
        if (existingStation == null || stationResult == 0) {
            Toast.makeText(this, getString(R.string.station_is_null, stationId), Toast.LENGTH_SHORT).show()
            finish()
        }

        existingStation = existingStation!!

        etName.setText(existingStation.name)
        etLocation.setText(existingStation.location)
        val (hiveCount, result) = StationsFunctionality.getHiveCount(db, stationId)
        if (result == 0) {
            Toast.makeText(this, getString(R.string.couldn_t_get_hive_count), Toast.LENGTH_SHORT).show()
            finish()
        }
        beehiveNum = hiveCount
        etHiveCount.setText(beehiveNum.toString())
        checkboxInUse.isChecked = existingStation.inUse == 1

        val btnSave = findViewById<Button>(R.id.btn_adjust_station_save)
        val btnBack = findViewById<Button>(R.id.btn_adjust_station_back)

        btnSave.setOnClickListener {
            saveChanges(existingStation)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun saveChanges(existingStation: Station) {
        val newName = etName.text.toString()
        val newLocation = etLocation.text.toString()
        val newHiveCount = etHiveCount.text.toString().toIntOrNull()
        val newInUseBool = checkboxInUse.isChecked
        val inUse = if (newInUseBool) 1 else 0
        var deathTime = Date(0)

        if (newHiveCount != null && Utils.correctHiveCount(newHiveCount) && beehiveNum <= newHiveCount) {
            // Proceed with your logic when the conditions are met
            val confirmationMessage = getString(R.string.are_you_sure_you_want_to_proceed)

            Utils.showConfirmationDialog(this, confirmationMessage) { confirmed ->
                if (confirmed) {
                    if (inUse == 0) {
                        val (hiveCount, stationResult) = StationsFunctionality.getHiveCount(db, existingStation.id)
                        when {
                            hiveCount > 0 -> {
                                Toast.makeText(this,
                                    getString(R.string.station_has_hives_can_t_discard), Toast.LENGTH_SHORT).show()
                                return@showConfirmationDialog
                            }
                            stationResult == 0 -> {
                                Toast.makeText(this,
                                    getString(R.string.cannot_retrieve_station), Toast.LENGTH_SHORT).show()
                                return@showConfirmationDialog
                            }
                            else -> {
                                deathTime = Date(System.currentTimeMillis())
                            }
                        }
                    }
                    val updatedStation = existingStation.copy(
                        name = newName,
                        location = newLocation,
                        inUse = inUse,
                        deathTime = deathTime
                    )

                    if (newHiveCount > beehiveNum) {
                        val newHives = newHiveCount - beehiveNum
                        StationsFunctionality.createHivesForStation(this, db, updatedStation, newHives)
                    }
                    StationsFunctionality.saveStation(this, db, updatedStation)
                    finish()
                } else {
                    return@showConfirmationDialog
                }
            }
        } else {
            Toast.makeText(this,
                getString(R.string.invalid_data_or_station_not_found), Toast.LENGTH_SHORT).show()
        }
    }

}
