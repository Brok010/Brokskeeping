package com.example.brokskeeping.NoteActivities

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.brokskeeping.DataClasses.HiveNotes
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.databinding.ActivityAddNoteBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: DatabaseHelper
    private var hiveId: Int = -1
    private var stationId: Int = -1
    private var date: Date = Date(0)
    private lateinit var etnoteText: EditText
    private lateinit var etnoteDate: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = DatabaseHelper(this)
        hiveId = intent.getIntExtra("hiveId", -1)
        stationId = intent.getIntExtra("stationId", -1)

        etnoteText = binding.noteText
        etnoteDate = binding.noteDate

        etnoteDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonSubmitNote.setOnClickListener {
            var noteText = etnoteText.text.toString()
            if (noteText == "") {
                Toast.makeText(this, "Invalid text", Toast.LENGTH_SHORT).show()
            } else {
                val note = HiveNotes(hiveId = hiveId, stationId = stationId, noteText = noteText, noteDate = date)
                NotesFunctionality.addNote(db, note)
                finish()
            }

        }

        binding.buttonDeleteNote.setOnClickListener {
            finish()
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
            etnoteDate.setText(formattedDate)
        }, year, month, day)
        datePickerDialog.show()
    }
}