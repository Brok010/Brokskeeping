package com.example.brokskeeping.ToDoActivities

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.example.brokskeeping.DataClasses.ToDo
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.databinding.ActivityAdjustToDoBinding
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdjustToDoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdjustToDoBinding
    private lateinit var db: DatabaseHelper
    private var toDoId: Int = -1
    private var newdate: Date = Date(0)
    private lateinit var btnToDoDatePicker: Button
    private lateinit var btnAdjust: Button
    private lateinit var stateCheckBox: CheckBox
    private lateinit var toDoText: TextView
    private lateinit var toDoDateText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdjustToDoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        toDoId = intent.getIntExtra("toDoId", -1)

        btnToDoDatePicker = binding.btnAdjustToDoDatePicker
        btnAdjust = binding.buttonAdjustToDo
        toDoText = binding.adjustToDoText
        toDoDateText = binding.adjustToDoDateText
        stateCheckBox = binding.adjustToDoCheckbox

        val toDo = ToDoFunctionality.getToDo(db, toDoId)

        toDoText.text = toDo.toDoText
        toDoDateText.text = toDo.date.toString()
        stateCheckBox.isChecked = toDo.toDoState

        btnToDoDatePicker.setOnClickListener {
            showDatePickerDialog()
        }
        btnAdjust.setOnClickListener {
            adjustToDo(toDo)
        }
    }


    private fun adjustToDo(toDo: ToDo) {
        // Retrieve the current values from UI components
        val newText = toDoText.text.toString()
        val newDateText = toDoDateText.text.toString()
        val newState = stateCheckBox.isChecked

        // Check if there are any changes
        if (newText != toDo.toDoText || newDateText != toDo.date.toString() || newState != toDo.toDoState) {
            val newToDo = ToDo(
                id = toDo.id,
                hiveId = toDo.hiveId,
                toDoText = newText,
                toDoState = newState,
                date = if (newdate != Date(0)) newdate else toDo.date
            )
            // Update the database
            ToDoFunctionality.adjustToDo(db, newToDo)
            finish()
        } else {
            Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show()
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
            newdate = selectedDate.time

            // Set the formatted date to the EditText
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(newdate)
            toDoDateText.text = formattedDate
        }, year, month, day)

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}