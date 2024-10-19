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
    private var date: Date = Date(0)
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

        //check if t0do has text - mandatory
        var toDoText = etToDoText.text.toString()
        if (toDoText == "") {
            Toast.makeText(this, "Invalid text", Toast.LENGTH_SHORT).show()

        // check if t0do date is in the future if no date was submitted do 6 days in future by default
        } else {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val futureDate = calendar.time
            var newdate = Date(0)
            val currentDate = Calendar.getInstance().time

            if (date != Date(0)) {  // if date was changed
                newdate = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).parse(tvToDoDate.text.toString())
            }
            // if wrong date or no date
            if (newdate.before(currentDate)) {
                newdate = futureDate
                Toast.makeText(this, "Wrong date", Toast.LENGTH_SHORT).show()
            }
            val toDo = ToDo(
                hiveId = hiveId,
                toDoText = toDoText,
                toDoState = false,
                date = newdate
            )
            ToDoFunctionality.addToDo(db, toDo)
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