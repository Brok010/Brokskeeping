package com.example.brokskeeping.HiveActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.R
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
            Toast.makeText(this, getString(R.string.hive_couldn_t_be_retrieved_from_db), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setValues(hive)

        binding.btnAdjustHive.setOnClickListener {
            startAdjustHiveActivity(hive.stationId, hiveId)
        }

        binding.btnDeleteHive.setOnClickListener {
            HivesFunctionality.deleteHive(db, hive.stationId, hiveId)
            Toast.makeText(this, getString(R.string.hive_deleted), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    override fun onResume() {
        super.onResume()
        reloadHiveData()
    }

    private fun reloadHiveData() {
        val (hive, result) = HivesFunctionality.getHiveAttributesById(db, hiveId)
        if (result == 0 || hive == null) {
            Toast.makeText(this, getString(R.string.hive_couldn_t_be_retrieved_from_db), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setValues(hive)
    }

    private fun setValues(hive: Beehive) = with(binding) {
        tvStationName.text = StationsFunctionality.getStationNameById(db, hive.stationId)
        tvHiveName.text = hive.nameTag ?: getString(R.string.no_name)
        tvQrTag.text = hive.qrTag ?: getString(R.string.no_qr_tag)
        tvBroodFrames.text = hive.broodFrames.toString()
        tvHoneyFrames.text = hive.honeyFrames.toString()
        tvFramesPerSuper.text = hive.framesPerSuper.toString()
        tvSupers.text = hive.supers.toString()
        tvDroneBroodFrames.text = hive.droneBroodFrames.toString()
        tvFreeSpaceFrames.text = hive.freeSpaceFrames.toString()
        tvColonyOrigin.text = hive.colonyOrigin ?: getString(R.string.no_origin)
        tvWinterReady.text = if (hive.winterReady) getString(R.string.yes) else getString(R.string.no)
        tvAggressivity.text = hive.aggressivity.toString()
        tvAttention.text = hive.attentionWorth.toString()
        if (hive.colonyEndState == 0) {
            tvDead.text = getString(R.string.yes)
        } else if (hive.colonyEndState == -1) {
            tvDead.text = getString(R.string.no)
        } else {
            val hiveName = HivesFunctionality.getHiveNameById(db, hive.colonyEndState)
            tvDead.text = getString(R.string.joined_with, hiveName)
        }
        tvStationOrder.text = hive.stationOrder.toString()

    }

    fun startAdjustHiveActivity(stationId: Int, hiveId: Int) {
        val intent = Intent(this, AdjustHiveActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }
}
