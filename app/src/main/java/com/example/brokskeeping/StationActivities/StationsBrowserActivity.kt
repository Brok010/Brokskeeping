package com.example.brokskeeping.StationActivities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.HiveActivities.HivesBrowserActivity
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding


class StationsBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var stationsAdapter: StationsAdapter
    private lateinit var header: TextView
    private lateinit var btnLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        stationsAdapter = StationsAdapter(mutableListOf(), db, this)

        // Bind views using ViewBinding
        header = binding.tvCommonBrowserHeader
        header.text = "My stations"

        // buttons
        btnLayout = binding.llCommonBrowserButtonLayout
        val addStationButton = Button(this).apply {
            id = View.generateViewId()
            text = "Add Station"
            setTextColor(ContextCompat.getColor(this@StationsBrowserActivity, R.color.buttonTextColor))
            backgroundTintList = ContextCompat.getColorStateList(this@StationsBrowserActivity, R.color.buttonColor)
        }
        btnLayout.addView(addStationButton)

        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@StationsBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = stationsAdapter
        }

        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(binding.commonFragmentContainer.id, fragment)
                .commit()
        }

        addStationButton.setOnClickListener {
            startAddStationActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        val (updatedStationList, result) = StationsFunctionality.getAllStations(db, 1)
        if (result == 0) {
            Toast.makeText(this, "Station loading was not successful", Toast.LENGTH_SHORT).show()
            finish()
        }
        stationsAdapter.updateData(updatedStationList)
    }

    fun startAddStationActivity() {
        val intent = Intent(this, AddStationActivity::class.java)
        startActivity(intent)
    }
    fun startStationActivity(stationId: Int) {
        val intent = Intent(this, StationActivity::class.java)
        intent.putExtra("stationId", stationId)
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