package com.example.brokskeeping.HiveActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.databinding.ActivityHiveBinding

class HiveActivity : AppCompatActivity() {
    private var hiveId: Int = -1
    private lateinit var db: DatabaseHelper
    private lateinit var binding: ActivityHiveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        hiveId = intent.getIntExtra("hiveId", -1)

        // Get hive attributes, handle null case upfront
        val (hive, result) = HivesFunctionality.getHiveAttributesById(db, hiveId)
        if (result == 0 || hive == null) {
            Toast.makeText(this, "Hive couldn't be retrieved from db", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setValues(hive)

        binding.btnAdjustHive.setOnClickListener {
            startAdjustHiveActivity(hive.stationId, hiveId)
        }

        binding.btnDeleteHive.setOnClickListener {
            HivesFunctionality.deleteHive(db, hive.stationId, hiveId)
            Toast.makeText(this, "Hive deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setValues(hive: Beehive) = with(binding) {
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
        tvAttention.text = hive.attentionWorth.toString()
        tvDead.text = if (hive.death) "Yes" else "No"
    }

    fun startAdjustHiveActivity(stationId: Int, hiveId: Int) {
        val intent = Intent(this, AdjustHiveActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }
}
