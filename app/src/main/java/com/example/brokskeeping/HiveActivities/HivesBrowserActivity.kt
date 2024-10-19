package com.example.brokskeeping.HiveActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.NoteActivities.NotesBrowserActivity
import com.example.brokskeeping.databinding.ActivityHivesBrowserBinding

class HivesBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHivesBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var hivesAdapter: HivesAdapter
    private var stationId: Int = -1
    private var stationName: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHivesBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        stationId = intent.getIntExtra("stationId", -1)
        db = DatabaseHelper(this)
        hivesAdapter = HivesAdapter(mutableListOf(), stationId, db, this)

        // get station name of db
        stationName = StationsFunctionality.getStationNameById(db, stationId)
        // Set the text of the TextView to the stationName
        val hivesLabel = "Hives of [$stationName]"
        binding.tvHivesHeader.text = hivesLabel

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HivesBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = hivesAdapter
        }

        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment)
                .commit()
        }

        // Set up the Floating Action Button (FAB) for adding a new station
        binding.AddHiveBt.setOnClickListener {
            startAddHiveActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        val updatedHivesList = HivesFunctionality.getAllHives(db, stationId)
        hivesAdapter.updateData(updatedHivesList)
    }

    fun startHiveActivity(stationId: Int, hiveId: Int) {
        val intent = Intent(this, NotesBrowserActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }

    fun startAddHiveActivity() {
        val intent = Intent(this, AddHiveActivity::class.java)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
    fun startAdjustHiveActivity(stationId: Int, hiveId: Int) {
        val intent = Intent(this, AdjustHiveActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }
}