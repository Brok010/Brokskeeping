//TODO: add checkbox into view

package com.example.brokskeeping.ToDoActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.ActivityToDoBrowserBinding

class ToDoBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityToDoBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var toDoAdapter: ToDoAdapter
    private var stationId: Int = -1
    private var hiveId: Int = -1
    private var state: Int = -1
    private var stationName: String = ""
    private var hiveName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)
        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        toDoAdapter = ToDoAdapter(mutableListOf(), db, this, hiveId)

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ToDoBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = toDoAdapter
        }

        //get stationName
        stationName = StationsFunctionality.getStationNameById(db, stationId)
        //get hiveName
        hiveName = HivesFunctionality.getHiveNameById(db, hiveId)

        val headerText = findViewById<TextView>(R.id.tv_to_do_header)
        val llStationFilter = findViewById<LinearLayout>(R.id.ll_station_filter)
        val llHiveFilter = findViewById<LinearLayout>(R.id.ll_hive_filter)

        if (stationId != -1 && hiveId != -1) {
            llStationFilter.visibility = View.GONE
            llHiveFilter.visibility = View.GONE
            headerText.text = "To Do's of hive [$hiveName]; [$stationName]"
        } else {
            headerText.text = "To Do's"
        }

        // Set up the Floating Action Button (FAB) for adding a new station
        binding.AddToDoBt.setOnClickListener {
            startAddToDoActivity()
        }
        // bottom menu
        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment)
                .commit()
        }

        binding.stateFilterInput.setText("All")
        if (stationId < 1) {
            binding.stationFilterInput.setText("All")
        }
        if (hiveId < 1) {
            binding.hiveFilterInput.setText("All")
        }


        binding.stationFilterInput.setOnClickListener {
            stationFilter()
        }

        binding.hiveFilterInput.setOnClickListener {
            hiveFilter()
        }

        binding.stateFilterInput.setOnClickListener {
            stateFilter()
        }

    }

    override fun onResume() {
        super.onResume()

        // Show or hide Add button based on selection
        if (stationId > 0 && hiveId > 0) {
            binding.AddToDoBt.visibility = View.VISIBLE
        } else {
            binding.AddToDoBt.visibility = View.GONE
        }

        val (updatedToDoList, result) = ToDoFunctionality.getAllToDos(db, hiveId, stationId, state, 0)
        if (result == 1) {
            toDoAdapter.updateData(updatedToDoList)
        } else {
            Toast.makeText(this, "ToDoUpdate didn't finish successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stationFilter() {
        val stations = StationsFunctionality.getAllStations(db)
        val stationNames = mutableListOf("All")
        val stationIds = mutableListOf(0)

        stations.forEach {
            stationNames.add(StationsFunctionality.getStationNameById(db, it.id))
            stationIds.add(it.id)
        }

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Choose a station")
        builder.setItems(stationNames.toTypedArray()) { _, which ->
            stationId = stationIds[which]
            hiveId = 0 // Reset to All since station changed
            binding.stationFilterInput.setText(stationNames[which])
            binding.hiveFilterInput.setText("All")
            onResume()
        }
        builder.show()
    }

    private fun hiveFilter() {
        if (stationId < 1) {
            Toast.makeText(this, "Please choose a station first.", Toast.LENGTH_SHORT).show()
        } else {
            val (hives, result) = HivesFunctionality.getAllHives(db, stationId)
            if (result != 1 || hives.isEmpty()) {
                Toast.makeText(this, "No hives found for this station.", Toast.LENGTH_SHORT).show()
            } else {
                val hiveNames = mutableListOf("All")
                val hiveIds = mutableListOf(0)

                hives.forEach {
                    hiveNames.add(HivesFunctionality.getHiveNameById(db, it.id))
                    hiveIds.add(it.id)
                }

                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Choose a hive")
                builder.setItems(hiveNames.toTypedArray()) { _, which ->
                    hiveId = hiveIds[which]
                    binding.hiveFilterInput.setText(hiveNames[which])
                    onResume()
                }
                builder.show()
            }
        }
    }

    fun stateFilter() {
        val options = arrayOf("All", "Done", "To be done")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Choose State")
        builder.setItems(options) { _, which ->
            state = when (which) {
                0 -> -1 // All
                1 -> 1  // Done
                2 -> 0  // To Be Done
                else -> -1
            }
            binding.stateFilterInput.setText(options[which])
            onResume()
        }
        builder.show()
    }

    fun startAddToDoActivity() {
        val intent = Intent(this, AddToDoActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }
    fun startAdjustToDoActivity(toDoId: Int) {
        val intent = Intent(this, AdjustToDoActivity::class.java)
        intent.putExtra("toDoId", toDoId)
        startActivity(intent)
    }

    fun startChangeStateToDoActivity(toDoId: Int) {
        ToDoFunctionality.toggleToDoState(db, toDoId)
        onResume()
    }
}