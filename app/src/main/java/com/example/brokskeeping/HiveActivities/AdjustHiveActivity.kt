package com.example.brokskeeping.HiveActivities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DataClasses.Station
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.R


class AdjustHiveActivity : AppCompatActivity() {
    private lateinit var etAdjustHiveNametag: EditText
    private lateinit var etAdjustHiveQRString: EditText
    private lateinit var btnSave: Button
    private lateinit var btnQRScanner: Button
    private lateinit var btnBack: Button
    private lateinit var spinner: Spinner
    private var stationId: Int = -1
    private var hiveId: Int = -1
    private var qrString = ""
    private lateinit var db: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjust_hive)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)
        db = DatabaseHelper(this)
        val stationAttributes = StationsFunctionality.getStationsAttributes(db, stationId)
        val stationName = stationAttributes?.name
        val (hive, result) = HivesFunctionality.getHiveAttributesById(db, hiveId)
        if (result == 0 || hive == null) {
            Toast.makeText(this, "Hive couldn't be retrieved from db", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val hiveName = hive.nameTag
        val hiveQrCode = hive.qrTag

        etAdjustHiveNametag = findViewById(R.id.et_adjust_hive_nametag)
        etAdjustHiveQRString = findViewById(R.id.et_adjust_hive_qr_string)
        btnSave = findViewById(R.id.btn_adjust_hive_save)
        btnBack = findViewById(R.id.btn_adjust_hive_back)
        btnQRScanner = findViewById(R.id.btn_scan_QR)
        spinner = findViewById(R.id.station_change_options)

        etAdjustHiveNametag.hint = hiveName
        etAdjustHiveQRString.hint = hiveQrCode

        //change stations
        val stations: List<Station> = StationsFunctionality.getAllStations(db)
        val stationNames: List<String> = stations.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stationNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Preselect the desired option
        val indexToSelect = stationNames.indexOf(stationName)
        if (indexToSelect >= 0) {
            spinner.setSelection(indexToSelect)
        }

        var selectedStationName: String? = null
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedStationName = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedStationName = null
            }
        }

        btnQRScanner.setOnClickListener {
            //TODO
        }

        btnSave.setOnClickListener {
            val newHiveName = etAdjustHiveNametag.text.toString()
            val newQRString = etAdjustHiveQRString.text.toString()

            val newStationId = if (selectedStationName != null) {
                StationsFunctionality.getStationIdByName(db, selectedStationName!!)
            } else {
                stationAttributes?.id
            }

            if (newStationId != null) {
                val beehive = Beehive(
                    id = hiveId,
                    stationId = newStationId,
                    nameTag = newHiveName,
                    qrTag = newQRString

//                broodFrames = broodFrames,
//                honeyFrames = honeyFrames,
//                framesPerSuper = framesPerSuper,
//                supers = superCount,
//                droneBroodFrames = droneFrames,
//                freeSpaceFrames = freeSpaceFrames,
//                supplementedFeedCount = hivesSupplementedFeedCount,
//                winterReady = winterReadyCheckbox.isChecked,
//                notes = notes
                )

                HivesFunctionality.updateHive(db, beehive)
            } else {
                Toast.makeText(this, "Invalid station selection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}