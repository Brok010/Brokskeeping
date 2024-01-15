package com.example.brokskeeping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.databinding.ActivityHivesBrowserBinding

class HivesBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHivesBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var hivesAdapter: HivesAdapter
    private var stationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHivesBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        stationId = intent.getIntExtra("stationId", -1)
        db = DatabaseHelper(this)
        hivesAdapter = HivesAdapter(mutableListOf(), stationId, db, this)

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HivesBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = hivesAdapter
        }

        // Set up the Floating Action Button (FAB) for adding a new station
        binding.AddStationBt.setOnClickListener {
            startAddHiveActivity()
        }

        //logout button
        val quitButton: Button = findViewById(R.id.quit_bt)
        quitButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val updatedStationList = db.getAllHives(stationId)
        hivesAdapter.updateData(updatedStationList)
    }

    fun startHiveActivity() {

    }

    fun startAddHiveActivity() {

    }
}