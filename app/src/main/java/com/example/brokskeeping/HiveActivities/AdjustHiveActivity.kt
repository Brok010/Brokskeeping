package com.example.brokskeeping.HiveActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.R

class AdjustHiveActivity : AppCompatActivity() {
    private lateinit var etAdjustHiveNametag: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    private var stationId: Int = -1
    private var hiveId: Int = -1
    private lateinit var db: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_hive)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)
        db = DatabaseHelper(this)

        etAdjustHiveNametag = findViewById(R.id.et_adjust_hive_nametag)
        btnSave = findViewById(R.id.btn_adjust_hive_save)
        btnBack = findViewById(R.id.btn_adjust_hive_back)

        btnSave.setOnClickListener {
            val hiveName = etAdjustHiveNametag.text.toString()
            HivesFunctionality.adjustHive(db, stationId, hiveId, hiveName)
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}