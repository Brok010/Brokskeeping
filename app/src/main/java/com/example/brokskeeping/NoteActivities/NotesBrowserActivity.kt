package com.example.brokskeeping.NoteActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.LogActivities.LogsBrowserActivity
import com.example.brokskeeping.R
import com.example.brokskeeping.ToDoActivities.ToDoActivity
import com.example.brokskeeping.ToDoActivities.ToDoBrowserActivity
import com.example.brokskeeping.databinding.ActivityNotesBrowserBinding


class NotesBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotesBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private var hiveId: Int = -1
    private var stationId: Int = -1
    private var stationName: String = ""
    private var hiveName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)

        db = DatabaseHelper(this)
        notesAdapter = NotesAdapter(mutableListOf(), hiveId, db, this)

        //get stationName
        stationName = StationsFunctionality.getStationNameById(db, stationId)
        //get hiveName
        hiveName = HivesFunctionality.getHiveNameById(db, hiveId)

        // Set the text of the TextView to the stationName
        binding.tvNotesHeader.text = "Notes of [$stationName], [$hiveName]"

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotesBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = notesAdapter
        }

        // Set up the Floating Action Button (FAB) for adding a new station
        binding.AddNoteBt.setOnClickListener {
            startAddNoteActivity()
        }

        //T0D0 button
        val browserToDoButton: Button = findViewById(R.id.to_do_bt)
        browserToDoButton.setOnClickListener {
            startToDoBrowserActivity()
        }

        //logs button
        val logsButton: Button = findViewById(R.id.logs_bt)
        logsButton.setOnClickListener {
            startLogsBrowserActivity()
        }

        //ToDoPopup
        val toDoList = ToDoFunctionality.getAllPendingToDos(db, hiveId)
        if (toDoList.isNotEmpty()) {
            startToDoActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        val updatedNotesList = NotesFunctionality.getAllNotes(db, hiveId)
        notesAdapter.updateData(updatedNotesList)
    }


    fun startLogsBrowserActivity() {
        val intent = Intent(this, LogsBrowserActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }

    fun startToDoBrowserActivity() {
        val intent = Intent(this, ToDoBrowserActivity()::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }

    fun startToDoActivity() {
        val intent = Intent(this, ToDoActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }

    fun startNoteActivity(noteId: Int) {
        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("noteId", noteId)
        startActivity(intent)
    }

    fun startAdjustNotesActivity(noteId: Int) {
        val intent = Intent(this, AdjustNoteActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("noteId", noteId)
        startActivity(intent)
    }

    fun startAddNoteActivity() {
        val intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
}