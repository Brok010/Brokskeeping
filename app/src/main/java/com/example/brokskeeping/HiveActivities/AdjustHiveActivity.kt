package com.example.brokskeeping.HiveActivities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.databinding.ActivityAdjustHiveBinding


class AdjustHiveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdjustHiveBinding
    private lateinit var db: DatabaseHelper
    private var stationId: Int = -1
    private var hiveId: Int = -1
    private var selectedStationName: String? = null
    private var stationAttributes: Station? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdjustHiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)
        db = DatabaseHelper(this)

        stationAttributes = StationsFunctionality.getStationsAttributes(db, stationId)
        val stationName = stationAttributes?.name

        val (hive, hiveResult) = HivesFunctionality.getHiveAttributesById(db, hiveId)
        if (hiveResult == 0 || hive == null) {
            Toast.makeText(this, "Hive couldn't be retrieved from db", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setValues(hive)

        val (stations, stationResult) = StationsFunctionality.getAllStations(db, 1)
        if (stationResult == 0) {
            Toast.makeText(this, "Station loading was not successful", Toast.LENGTH_SHORT).show()
            finish()
        }

        val stationNames = stations.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stationNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stationChangeOptions.adapter = adapter

        val indexToSelect = stationNames.indexOf(stationName)
        if (indexToSelect >= 0) {
            binding.stationChangeOptions.setSelection(indexToSelect)
        }

//        var selectedStationName: String? = null
        binding.stationChangeOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedStationName = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedStationName = null
            }
        }

        binding.btnScanQR.setOnClickListener {
            // TODO: Handle QR scan
        }

        binding.btnAdjustHiveSave.setOnClickListener {
            saveValues()
        }

        binding.btnAdjustHiveBack.setOnClickListener {
            finish()
        }
    }


    private fun saveValues() = with(binding) {
        val newHiveName = etAdjustHiveNametag.text.toString()
        val newQRString = etAdjustHiveQrString.text.toString()

        val newStationId = selectedStationName?.let {
            StationsFunctionality.getStationIdByName(db, it)
        } ?: stationAttributes?.id

        if (newStationId == null) {
            Toast.makeText(this@AdjustHiveActivity, "Invalid station selection", Toast.LENGTH_SHORT).show()
            return
        }

//        val colonyEndState = if (checkboxDead.isChecked) -> 0, else -1

        val beehive = Beehive(
            id = hiveId,
            stationId = newStationId,
            nameTag = newHiveName,
            qrTag = newQRString,
            broodFrames = tvBroodFrames.text.toString().toIntOrNull() ?: 0,
            honeyFrames = etHoneyFrames.text.toString().toIntOrNull() ?: 0,
            droneBroodFrames = etDroneBroodFrames.text.toString().toIntOrNull() ?: 0,
            framesPerSuper = etFramesPerSuper.text.toString().toIntOrNull() ?: 0,
            supers = etSupers.text.toString().toIntOrNull() ?: 0,
            freeSpaceFrames = 0, // Set this based on app logic if needed
            colonyOrigin = etColonyOrigin.text.toString(),
//            colonyEndState = colonyEndState,
            supplementedFeedCount = etSupplementedFeed.text.toString().toIntOrNull() ?: 0,
            winterReady = checkboxWinterReady.isChecked,
            aggressivity = sliderAggressivitySlider.value.toInt(),
            attentionWorth = sliderAttentionWorth.value.toInt()
        )

        HivesFunctionality.updateHive(db, beehive)
        finish()
    }
    private fun setValues(hive: Beehive) = with(binding) {
        etAdjustHiveNametag.setText(hive.nameTag)
        etAdjustHiveQrString.setText(hive.qrTag)
        tvBroodFrames.setText(hive.broodFrames.toString())
        etHoneyFrames.setText(hive.honeyFrames.toString())
        etDroneBroodFrames.setText(hive.droneBroodFrames.toString())
        etFramesPerSuper.setText(hive.framesPerSuper.toString())
        etSupers.setText(hive.supers.toString())
        etColonyOrigin.setText(hive.colonyOrigin)
        etSupplementedFeed.setText(hive.supplementedFeedCount.toString())
        checkboxWinterReady.isChecked = hive.winterReady
        sliderAggressivitySlider.value = hive.aggressivity.toFloat().coerceIn(1.0f, 5.0f)
        sliderAttentionWorth.value = hive.attentionWorth.toFloat().coerceIn(1.0f, 5.0f)
    }
}