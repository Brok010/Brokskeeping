package com.example.brokskeeping

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DiscardedEntitiesActivities.DiscardedEntitiesBrowserActivity
import com.example.brokskeeping.Functionality.Reused_functions
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.HoneyHarvestActivities.HoneyHarvestBrowserActivity
import com.example.brokskeeping.InspectionActivities.InspectionsBrowserActivity
import com.example.brokskeeping.StationActivities.StationsBrowserActivity
import com.example.brokskeeping.SupplementedFeedActivities.SupplementedFeedBrowserActivity
import com.example.brokskeeping.ToDoActivities.ToDoBrowserActivity

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        Utils.applySavedLocale(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        // Get references to the buttons
        val btnDatabase: Button = findViewById(R.id.btn_database)
        val btnInspections: Button = findViewById(R.id.btn_inspections)
        val btnHoneyHarvest: Button = findViewById(R.id.btn_honey_harvest)
        val btnSupplementedFeed: Button = findViewById(R.id.btn_Supplemented_feed)
        val btnToDo: Button = findViewById(R.id.btn_to_do)
        val btnDiscardedEntities: Button = findViewById(R.id.btn_discarded_entities)
        val btnSettings: ImageButton = findViewById(R.id.btn_settings)

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

        btnSupplementedFeed.setOnClickListener {
            val intent = Intent(this, SupplementedFeedBrowserActivity::class.java)
            startActivity(intent)
        }

        btnToDo.setOnClickListener {
            val intent = Intent(this, ToDoBrowserActivity::class.java)
            startActivity(intent)
        }

        btnDiscardedEntities.setOnClickListener {
            val intent = Intent(this, DiscardedEntitiesBrowserActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            Reused_functions.showSettingsMenu(this, it, db)
        }

    }
}
