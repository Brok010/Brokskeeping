package com.example.brokskeeping.HiveActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.InspectionActivities.InspectionActivity
import com.example.brokskeeping.InspectionDataActivities.HiveInspectionDataBrowserActivity
import com.example.brokskeeping.LogActivities.LogsBrowserActivity
import com.example.brokskeeping.NoteActivities.NotesBrowserActivity
import com.example.brokskeeping.R
import com.example.brokskeeping.ToDoActivities.ToDoBrowserActivity
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding

class HivesBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var hivesAdapter: HivesAdapter
    private var stationId: Int = -1
    private var stationName: String = ""
    private lateinit var header: TextView
    private lateinit var btnLayout: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        stationId = intent.getIntExtra("stationId", -1)
        db = DatabaseHelper(this)
        hivesAdapter = HivesAdapter(mutableListOf(), stationId, db, this)

        // get station name of db
        stationName = StationsFunctionality.getStationNameById(db, stationId)
        // Set the text of the TextView to the stationName
        val hivesLabel = "Hives of [$stationName]"
        // Bind views using ViewBinding
        header = binding.tvCommonBrowserHeader
        header.text = hivesLabel

        // buttons
        btnLayout = binding.llCommonBrowserButtonLayout
        val addHiveButton = Button(this).apply {
            id = View.generateViewId()
            text = "Add Hive"
            setTextColor(ContextCompat.getColor(this@HivesBrowserActivity, R.color.buttonTextColor))
            backgroundTintList = ContextCompat.getColorStateList(this@HivesBrowserActivity, R.color.buttonColor)
        }
        val newInspectionButton = Button(this).apply {
            id = View.generateViewId()
            text = "New Inspection"
            setTextColor(ContextCompat.getColor(this@HivesBrowserActivity, R.color.buttonTextColor))
            backgroundTintList = ContextCompat.getColorStateList(this@HivesBrowserActivity, R.color.buttonColor)
        }
        btnLayout.addView(newInspectionButton)
        btnLayout.addView(addHiveButton)

        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HivesBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = hivesAdapter
        }

        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(binding.commonFragmentContainer.id, fragment)
                .commit()
        }

        // Set up the Floating Action Button (FAB) for adding a new station
        addHiveButton.setOnClickListener {
            startAddHiveActivity()
        }

        newInspectionButton.setOnClickListener {
            startNewInspectionActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        val (updatedHivesList, result) = HivesFunctionality.getAllHives(db, stationId, 0, ordered = true)
        if (result == 1) {
            hivesAdapter.updateData(updatedHivesList)
        } else {
            Toast.makeText(this, "HivesUpdateData did not finish successfully", Toast.LENGTH_SHORT).show()
        }
    }

    fun startHiveInspectionDataBrowserActivity(hiveId: Int) {
        val intent = Intent(this, HiveInspectionDataBrowserActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }

    fun startHiveActivity(hiveId: Int) {
        val intent = Intent(this, HiveActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }



    fun startNotesBrowserActivity(stationId: Int, hiveId: Int) {
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

    fun startNewInspectionActivity() {
        val intent = Intent(this, InspectionActivity::class.java)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }

    fun startLogsBrowserActivity(hiveId: Int) {
        val intent = Intent(this, LogsBrowserActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }

    fun startToDoBrowserActivity(hiveId: Int) {
        val intent = Intent(this, ToDoBrowserActivity()::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
}