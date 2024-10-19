package com.example.brokskeeping.StationActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R

class AddStationActivity : AppCompatActivity() {
    private lateinit var editTextStationName: EditText
    private lateinit var editTextStationLocation: EditText
    private lateinit var editTextBeehiveNumber: EditText
    private lateinit var buttonSaveStation: Button
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_station)

        // Initialize UI elements
        editTextStationName = findViewById(R.id.editTextStationName)
        editTextStationLocation = findViewById(R.id.editTextStationLocation)
        editTextBeehiveNumber = findViewById(R.id.editTextBeehiveNumber)
        buttonSaveStation = findViewById(R.id.buttonSaveStation)
        db = DatabaseHelper(this)

        // Set click listener for the save button
        buttonSaveStation.setOnClickListener {
            saveStation()
        }
    }

    private fun saveStation() {
        val stationName = editTextStationName.text.toString()
        val stationLocation = editTextStationLocation.text.toString()
        val beehiveNumberText = editTextBeehiveNumber.text.toString()

        // Check if the beehive number is a valid number between 0 and 100
        if (beehiveNumberText.isNotBlank() && beehiveNumberText.toIntOrNull() != null && stationName.isNotBlank()) {
            val beehiveNumber = beehiveNumberText.toInt()
            if (Utils.correctHiveCount(beehiveNumber)) {
                // Beehive number is valid, process the information
                processStationInformation(stationName, stationLocation, beehiveNumber)
            } else {
                // Invalid beehive number, show a message to the user
                Toast.makeText(this, "Please enter a valid beehive number between 1 and 100 and station name", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Invalid input, show a message to the user
            Toast.makeText(this, "Please enter a valid beehive number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processStationInformation(stationName: String, stationLocation: String, beehiveNumber: Int) {
        val newStation = Station(name = stationName, location = stationLocation, beehiveNum = beehiveNumber)
        StationsFunctionality.saveStation(db, newStation)

        // You can add additional logic as needed
        Toast.makeText(this, "Station saved successfully", Toast.LENGTH_SHORT).show()
        finish() // Close the activity after saving
    }
}