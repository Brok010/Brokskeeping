package com.example.brokskeeping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.databinding.ActivityNotesBrowserBinding


class NotesBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotesBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private var hiveNameTag: String = ""
    private var hiveId: Int = -1
    private var stationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)
        hiveNameTag = intent.getStringExtra("hiveNameTag") ?: "Default Name"

        db = DatabaseHelper(this)
        notesAdapter = NotesAdapter(mutableListOf(), hiveId, db, this)

        // Set the text of the TextView to the stationName
        binding.tvHiveLabel.text = hiveNameTag

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
        val logsButton: Button = findViewById(R.id.logs_bt)
        logsButton.setOnClickListener {
            startLogsBrowserActivity(hiveId)
        }
    }


    fun startLogsBrowserActivity(hiveId: Int) {
        val intent = Intent(this, LogsBrowserActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }

    fun startNoteActivity(noteId: Int) {
        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }

    fun startAdjustNotesActivity(noteId: Int) {
        val intent = Intent(this, AdjustNoteActivity::class.java)
        intent.putExtra("noteId", noteId)
        startActivity(intent)
    }

    fun startAddNoteActivity() {
        val intent = Intent(this, AddNoteActivity::class.java)
        startActivity(intent)
    }
}