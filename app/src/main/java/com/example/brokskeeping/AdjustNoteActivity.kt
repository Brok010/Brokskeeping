package com.example.brokskeeping

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.brokskeeping.Classes.HiveNotes
import com.example.brokskeeping.databinding.ActivityAdjustNoteBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AdjustNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdjustNoteBinding
    private lateinit var db: DatabaseHelper
    private var hiveId: Int = -1
    private var noteId: Int = -1
    private var stationId: Int = -1
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private lateinit var noteText: EditText
    private lateinit var noteDate: EditText
    private lateinit var calendar: Calendar
    private var date: Date = Date(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdjustNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hiveId = intent.getIntExtra("hiveId", -1)
        noteId = intent.getIntExtra("noteId", -1)
        stationId = intent.getIntExtra("stationId", -1)

        db = DatabaseHelper(this)

        saveButton = binding.btnAdjustNoteSave
        backButton = binding.btnAdjustNoteBack
        noteText = binding.etAdjustNoteText
        noteDate = binding.etAdjustNoteDate
        calendar = Calendar.getInstance()

        noteDate.setOnClickListener {
            showDatePickerDialog()
        }

        saveButton.setOnClickListener {
            var text = noteText.text.toString()
            if (text == "" && date == Date(0)) {
                Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show()

            } else { //some data are adjusted
                var originalNote = NotesFunctionality.getNote(db, noteId)
                if (date == Date(0)) {
                    date = originalNote.noteDate
                }
                if (text == "") {
                    text = originalNote.noteText
                }
                val updatedNote = HiveNotes(id = noteId, hiveId = hiveId, stationId = stationId, noteText = text, noteDate = date)
                NotesFunctionality.adjustNote(db, updatedNote)
                finish()
            }
        }

        backButton.setOnClickListener {
            finish() // Close the activity
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, monthOfYear)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Convert the selected date to a Date object
            date = selectedDate.time

            // Set the formatted date to the EditText
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(date)
            noteDate.setText(formattedDate)
        }, year, month, day)
        datePickerDialog.show()
    }
}