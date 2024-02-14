package com.example.brokskeeping

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.brokskeeping.DataClasses.ToDo
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.databinding.ActivityNotesBrowserBinding
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

        var toDoList = ToDoFunctionality.getAllPendingToDos(db, hiveId)
        var toDoId = showFirstToDo(toDoList)

        binding.toDoSkip.setOnClickListener {
            if (toDoList.isNotEmpty()) {
                toDoList = toDoList.drop(1)

                if (toDoList.isNotEmpty()) {
                    toDoId = showFirstToDo(toDoList)
                } else {
                    finish()
                }
            }
        }

        binding.toDoDone.setOnClickListener {
            if (toDoList.isNotEmpty()) {
                toDoList = toDoList.drop(1)
                ToDoFunctionality.setToDone(db, toDoId)

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