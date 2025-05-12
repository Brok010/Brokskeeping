package com.example.brokskeeping.ToDoActivities

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.brokskeeping.DataClasses.HiveNotes
import com.example.brokskeeping.DataClasses.ToDo
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.databinding.ActivityAddToDoBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddToDoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddToDoBinding
    private lateinit var db: DatabaseHelper
    private var hiveId: Int = -1
    private var date: Date? = null
    private lateinit var etToDoText: EditText
    private lateinit var tvToDoDate: TextView
    private lateinit var btnToDoDatePicker: Button
    private lateinit var buttonSubmitToDo: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddToDoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        hiveId = intent.getIntExtra("hiveId", -1)

        etToDoText = binding.toDoText
        tvToDoDate = binding.toDoDateText
        btnToDoDatePicker = binding.btnToDoDatePicker
        buttonSubmitToDo = binding.buttonSubmitToDo

        btnToDoDatePicker.setOnClickListener {
            showDatePickerDialog()
        }

        buttonSubmitToDo.setOnClickListener {
            submitToDo()
        }
    }


    private fun submitToDo() {
        val toDoText = etToDoText.text.toString().trim()
        if (toDoText.isEmpty()) {
            Toast.makeText(this, "Please enter a to-do description.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val defaultFutureDate = calendar.time

        // Use selected date if valid, otherwise default
        val selectedDate = if (date != null && date!!.after(currentDate)) {
            date!!
        } else {
            Toast.makeText(this, "Date is invalid or in the past. Defaulting to 6 days in future.", Toast.LENGTH_SHORT).show()
            defaultFutureDate
        }

        val toDo = ToDo(
            hiveId = hiveId,
            toDoText = toDoText,
            toDoState = false,
            date = selectedDate
        )

        val result = ToDoFunctionality.addToDo(db, toDo)
        if (result == 0) {
            Toast.makeText(this, "Error adding to-do to database.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "To-do added successfully.", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun showDatePickerDialog() { // add minimal date to be today
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
            tvToDoDate.text = formattedDate
        }, year, month, day)

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}