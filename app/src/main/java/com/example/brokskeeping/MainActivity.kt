package com.example.brokskeeping

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.brokskeeping.HoneyHarvestActivities.HoneyHarvestBrowserActivity
import com.example.brokskeeping.InspectionActivities.InspectionsBrowserActivity
import com.example.brokskeeping.StationActivities.StationsBrowserActivity
import com.example.brokskeeping.ToDoActivities.ToDoBrowserActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get references to the buttons
        val btnDatabase: Button = findViewById(R.id.btn_database)
        val btnInspections: Button = findViewById(R.id.btn_inspections)
        val btnHoneyHarvest: Button = findViewById(R.id.btn_honey_harvest)
        val btnToDo: Button = findViewById(R.id.btn_to_do)

        // Set onClick listeners for each button
        btnDatabase.setOnClickListener {
            val intent = Intent(this, StationsBrowserActivity::class.java)
            startActivity(intent)
        }

        btnInspections.setOnClickListener {
            val intent = Intent(this, InspectionsBrowserActivity::class.java)
            startActivity(intent)
        }

        btnHoneyHarvest.setOnClickListener {
            val intent = Intent(this, HoneyHarvestBrowserActivity::class.java)
            startActivity(intent)
        }

        btnToDo.setOnClickListener {//get all todo's - filter by station and implement the popup
            val intent = Intent(this, ToDoBrowserActivity::class.java)
            startActivity(intent)
        }
    }
}
