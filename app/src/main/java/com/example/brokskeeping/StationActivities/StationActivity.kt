package com.example.brokskeeping.StationActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.HiveActivities.AdjustHiveActivity
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.ActivityHiveBinding
import com.example.brokskeeping.databinding.ActivityStationBinding

class StationActivity : AppCompatActivity() {
    private var stationId: Int = -1
    private lateinit var db: DatabaseHelper
    private lateinit var binding: ActivityStationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        stationId = intent.getIntExtra("stationId", -1)

        val (station, result) = StationsFunctionality.getStationsAttributes(db, stationId)
        if (result == 0 || station == null) {
            Toast.makeText(this,
                getString(R.string.station_couldn_t_be_retrieved_from_db), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvStationName.text = station.name
        binding.tvLocation.text = station.location
        binding.tvInUse.text = if (station.inUse == 1) "Yes" else "No"

        binding.btnAdjustStation.setOnClickListener {
            startAdjustStationActivity(stationId)
        }

        binding.btnDeleteStation.setOnClickListener {
            StationsFunctionality.deleteStation(db, stationId)
            Toast.makeText(this, getString(R.string.station_deleted), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun startAdjustStationActivity(stationId: Int) {
        val intent = Intent(this, AdjustStationActivity::class.java)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
}