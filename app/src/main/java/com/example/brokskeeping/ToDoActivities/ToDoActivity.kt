package com.example.brokskeeping.ToDoActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.brokskeeping.DataClasses.ToDo
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.ActivityToDoBinding

class ToDoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityToDoBinding
    private lateinit var tvToDoText: TextView
    private lateinit var tvToDoDate: TextView
    private var hiveId: Int = -1
    private lateinit var db: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hiveId = intent.getIntExtra("hiveId", -1)
        db = DatabaseHelper(this)

        tvToDoText = findViewById(R.id.to_do_text)
        tvToDoDate = findViewById(R.id.to_do_date)

        var (toDoList, _) = ToDoFunctionality.getAllToDos(db, hiveId, 0, 0, 1)
        var toDoId = showFirstToDo(toDoList)

        binding.toDoDone.setOnClickListener {
            if (toDoList.isNotEmpty()) {
                toDoList = toDoList.drop(1)
                ToDoFunctionality.toggleToDoState(db, toDoId)

                if (toDoList.isNotEmpty()) {
                    toDoId = showFirstToDo(toDoList)
                } else {
                    finish()
                }
            }
        }

        binding.toDoPostpone.setOnClickListener {
            if (toDoList.isNotEmpty()) {
                toDoList = toDoList.drop(1)
                ToDoFunctionality.postpone(db ,toDoId)

                if (toDoList.isNotEmpty()) {
                    toDoId = showFirstToDo(toDoList)
                } else {
                    finish()
                }
            }
        }
    }

    private fun showFirstToDo(toDoList: List<ToDo>): Int {
        val first = toDoList[0]
        tvToDoDate.text = first.date.toString()
        tvToDoText.text = first.toDoText
        return first.id
    }
}