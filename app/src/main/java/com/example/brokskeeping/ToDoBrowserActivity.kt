//TODO: add checkbox into view

package com.example.brokskeeping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.databinding.ActivityToDoBrowserBinding

class ToDoBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityToDoBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var toDoAdapter: ToDoAdapter
    private var hiveId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Set up the Floating Action Button (FAB) for adding a new station
        binding.AddToDoBt.setOnClickListener {
            startAddToDoActivity()
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