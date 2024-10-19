package com.example.brokskeeping.NoteActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.databinding.ActivityNoteBinding

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var hiveId: Int = -1
    private var noteId: Int = -1
    private var stationId: Int = -1
    private lateinit var db: DatabaseHelper
    private lateinit var noteText: TextView
    private lateinit var noteDate: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hiveId = intent.getIntExtra("hiveId", -1)
        noteId = intent.getIntExtra("noteId", -1)
        stationId = intent.getIntExtra("stationId", -1)
        db = DatabaseHelper(this)

        noteText = binding.noteTextTv
        noteDate = binding.noteDateTv

        val currentNote = NotesFunctionality.getNote(db, noteId)
        noteText.text = currentNote.noteText
        noteDate.text = currentNote.noteDate.toString()

        binding.AdjustBt.setOnClickListener {
            startAdjustNotesActivity(noteId)
        }
    }

    private fun startAdjustNotesActivity(noteId: Int) {
        val intent = Intent(this, AdjustNoteActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("noteId", noteId)
        startActivity(intent)
    }
}