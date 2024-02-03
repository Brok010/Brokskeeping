package com.example.brokskeeping

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.DbFunctionality.Utils

class AdjustStationActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private var stationId: Int = -1
    private lateinit var etName: EditText
    private lateinit var etLocation: EditText
    private lateinit var etHiveCount: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_station)

        db = DatabaseHelper(this)

        // Retrieve stationId from the intent
        stationId = intent.getIntExtra("stationId", -1)

        etName = findViewById(R.id.et_adjust_station_name)
        etLocation = findViewById(R.id.et_adjust_station_location)
        etHiveCount = findViewById(R.id.et_adjust_station_hive_count)

        // Load existing station data
        var existingStation = StationsFunctionality.getStationsAttributes(db, stationId)
        if (existingStation == null) {
            Toast.makeText(this, "Station is null $stationId", Toast.LENGTH_SHORT).show()
            finish()
        }

        existingStation = existingStation!!

        etName.setText(existingStation.name)
        etLocation.setText(existingStation.location)
        etHiveCount.setText(existingStation.beehiveNum.toString())

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
        var newHiveCount = etHiveCount.text.toString().toIntOrNull()

        if (existingStation != null && newHiveCount != null && Utils.correctHiveCount(newHiveCount) && existingStation.beehiveNum <= newHiveCount) {
            // Proceed with your logic when the conditions are met
            val confirmationMessage = "Are you sure you want to proceed?"

            Utils.showConfirmationDialog(this, confirmationMessage) { confirmed ->
                if (confirmed) {
                    // User clicked "confirmed"
                    val updatedStation = existingStation.copy(
                        name = newName,
                        location = newLocation,
                        beehiveNum = newHiveCount
                    )

                    if (newHiveCount > existingStation.beehiveNum) {
                        val newHives = newHiveCount - existingStation.beehiveNum
                        StationsFunctionality.createHivesForStation(db, stationId, newHives)
                    }
                    StationsFunctionality.adjustStation(db, stationId, updatedStation)
                    finish()
                } else {
                    // not confirmed, dialog window closes
                }
            }
        } else {
            Toast.makeText(this, "Invalid data or station not found", Toast.LENGTH_SHORT).show()
        }
    }

}
