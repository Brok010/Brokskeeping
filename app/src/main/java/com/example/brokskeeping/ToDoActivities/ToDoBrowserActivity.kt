//TODO: add checkbox into view

package com.example.brokskeeping.ToDoActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.databinding.ActivityToDoBrowserBinding

class ToDoBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityToDoBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var toDoAdapter: ToDoAdapter
    private var stationId: Int = -1
    private var hiveId: Int = -1
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
        binding.tvToDoHeader.text = "To Do's of [$stationName], [$hiveName]"

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
    }

    override fun onResume() {
        super.onResume()
        val updatedStationList = ToDoFunctionality.getAllToDos(db, hiveId)
        toDoAdapter.updateData(updatedStationList)
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
}