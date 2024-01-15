package com.example.brokskeeping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.databinding.ActivityStationsBrowserBinding

class StationsBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStationsBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var stationsAdapter: StationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationsBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        stationsAdapter = StationsAdapter(mutableListOf(), db, this)

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StationsBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = stationsAdapter
        }

        // Set up the Floating Action Button (FAB) for adding a new station
        binding.AddStationBt.setOnClickListener {
            startAddStationActivity()
        }

        //logout button
        val quitButton: Button = findViewById(R.id.quit_bt)
        quitButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val updatedStationList = db.getAllStations()
        stationsAdapter.updateData(updatedStationList)
    }

    fun startAddStationActivity() {
        val intent = Intent(this, AddStationActivity::class.java)
        startActivity(intent)
    }
    fun startBrowseHivesActivity(stationName: String, stationId: Int) {
        val intent = Intent(this, HivesBrowserActivity::class.java)
        intent.putExtra("stationName", stationName)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
}