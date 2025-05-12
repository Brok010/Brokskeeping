package com.example.brokskeeping.HiveActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.R

class HiveActivity : AppCompatActivity() {
    private var hiveId: Int = -1
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hive)

        db = DatabaseHelper(this)
        hiveId = intent.getIntExtra("hiveId", -1)

        // Get hive attributes, handle null case upfront
        val (hive, result) = HivesFunctionality.getHiveAttributesById(db, hiveId)
        if (result == 0 || hive == null) {
            Toast.makeText(this, "Hive couldn't be retrieved from db", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Binding views
        val btnAdjustHive = findViewById<Button>(R.id.btn_adjust_hive)
        val btnDeleteHive = findViewById<Button>(R.id.btn_delete_hive)

        val tvStationName = findViewById<TextView>(R.id.tv_station_name)
        val tvHiveName = findViewById<TextView>(R.id.tv_hive_name)
        val tvQrTag = findViewById<TextView>(R.id.tv_qr_tag)
        val tvBroodFrames = findViewById<TextView>(R.id.tv_brood_frames)
        val tvHoneyFrames = findViewById<TextView>(R.id.tv_honey_frames)
        val tvFramesPerSuper = findViewById<TextView>(R.id.tv_frames_per_super)
        val tvSupers = findViewById<TextView>(R.id.tv_supers)
        val tvDroneBroodFrames = findViewById<TextView>(R.id.tv_drone_brood_frames)
        val tvFreeSpaceFrames = findViewById<TextView>(R.id.tv_free_space_frames)
        val tvColonyOrigin = findViewById<TextView>(R.id.tv_colony_origin)
        val tvSupplementedFeedCount = findViewById<TextView>(R.id.tv_supplemented_feed_count)
        val tvWinterReady = findViewById<TextView>(R.id.tv_winter_ready)
        val tvAggressivity = findViewById<TextView>(R.id.tv_aggressivity)

        // Set TextView values with default fallback using Elvis operator
        tvStationName.text = StationsFunctionality.getStationNameById(db, hive.stationId) ?: "Unknown"
        tvHiveName.text = hive.nameTag ?: "No Name"
        tvQrTag.text = hive.qrTag ?: "No QR Tag"
        tvBroodFrames.text = hive.broodFrames.toString()
        tvHoneyFrames.text = hive.honeyFrames.toString()
        tvFramesPerSuper.text = hive.framesPerSuper.toString()
        tvSupers.text = hive.supers.toString()
        tvDroneBroodFrames.text = hive.droneBroodFrames.toString()
        tvFreeSpaceFrames.text = hive.freeSpaceFrames.toString()
        tvColonyOrigin.text = hive.colonyOrigin ?: "No Origin"
        tvSupplementedFeedCount.text = hive.supplementedFeedCount.toString()
        tvWinterReady.text = if (hive.winterReady) "Yes" else "No"
        tvAggressivity.text = hive.aggressivity.toString()

        btnAdjustHive.setOnClickListener {
            startAdjustHiveActivity(hive.stationId, hiveId)
        }

        btnDeleteHive.setOnClickListener {
            HivesFunctionality.deleteHive(db, hive.stationId, hiveId)
            Toast.makeText(this, "Hive deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun startAdjustHiveActivity(stationId: Int, hiveId: Int) {
        val intent = Intent(this, AdjustHiveActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }
}
