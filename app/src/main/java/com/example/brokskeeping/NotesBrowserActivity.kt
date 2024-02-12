package com.example.brokskeeping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.databinding.ActivityNotesBrowserBinding


class NotesBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotesBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private var hiveId: Int = -1
    private var stationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)

        db = DatabaseHelper(this)
        notesAdapter = NotesAdapter(mutableListOf(), hiveId, db, this)

        // Set the text of the TextView to the stationName
        binding.tvNotesStation.text = "Station $stationId Hive $hiveId"

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

        //quit button
        val addToDoButton: Button = findViewById(R.id.logs_bt)
        addToDoButton.setOnClickListener {
            startAddToDoActivity()
        }

        //quit button
        val logsButton: Button = findViewById(R.id.logs_bt)
        logsButton.setOnClickListener {
            startLogsBrowserActivity()
        }
        startToDoActivity()
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

    fun startAddToDoActivity() {
        val intent = Intent(this, AddToDo::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }

    fun startToDoActivity() {
        val intent = Intent(this, ToDoActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
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