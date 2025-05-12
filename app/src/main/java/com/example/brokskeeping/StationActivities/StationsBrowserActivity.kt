package com.example.brokskeeping.StationActivities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.HiveActivities.HivesBrowserActivity
import com.example.brokskeeping.HoneyHarvestActivities.HoneyHarvestBrowserActivity
import com.example.brokskeeping.InspectionActivities.InspectionsBrowserActivity
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

        // bottom menu TODO add in a hive id paramentr to send to the bottommenufragment class
        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment)
                .commit()
        }

        binding.AddStationBt.setOnClickListener {
            startAddStationActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        val updatedStationList = StationsFunctionality.getAllStations(db)
        stationsAdapter.updateData(updatedStationList)
    }

    fun startAddStationActivity() {
        val intent = Intent(this, AddStationActivity::class.java)
        startActivity(intent)
    }
    fun startBrowseHivesActivity(stationName: String, stationId: Int) {
        val intent = Intent(this, HivesBrowserActivity::class.java)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
    fun startAdjustStationActivity(stationId: Int) {
        val intent = Intent(this, AdjustStationActivity::class.java)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
}