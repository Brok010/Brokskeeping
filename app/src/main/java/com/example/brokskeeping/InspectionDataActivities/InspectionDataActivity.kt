package com.example.brokskeeping.InspectionDataActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.ActivityInspectionDataBinding

class InspectionDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInspectionDataBinding
    private lateinit var db: DatabaseHelper
    private var inspectionDataId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInspectionDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inspectionDataId = intent.getIntExtra("inspectionDataId", -1)
        db = DatabaseHelper(this)

        val (currentInspectionData, result) = InspectionsFunctionality.getAllInspectionDataById(db, inspectionDataId)
        if (result != 1 && currentInspectionData != null) {
            Toast.makeText(this, "InspectionDataLoad not successful - See logs for details", Toast.LENGTH_SHORT).show()
            finish()
        }

        // checkboxes
        val ivBroodFrame = findViewById<ImageView>(R.id.iv_brood_frames)
        ivBroodFrame.setImageResource(
            if (currentInspectionData?.broodFramesAdjusted == true) R.drawable.ic_yes else R.drawable.ic_no
        )
        val ivDroneBroodFrame = findViewById<ImageView>(R.id.iv_drone_frames)
        ivDroneBroodFrame.setImageResource(
            if (currentInspectionData?.droneBroodFramesAdjusted == true) R.drawable.ic_yes else R.drawable.ic_no
        )
        val ivHoneyFrame = findViewById<ImageView>(R.id.iv_honey_frames)
        ivHoneyFrame.setImageResource(
            if (currentInspectionData?.honeyFramesAdjusted == true) R.drawable.ic_yes else R.drawable.ic_no
        )
        val ivSupplementedFeed = findViewById<ImageView>(R.id.iv_supplemented_feed)
        ivSupplementedFeed.setImageResource(
            if (currentInspectionData?.supplementedFeed == true) R.drawable.ic_yes else R.drawable.ic_no
        )
        val ivWinterReady = findViewById<ImageView>(R.id.iv_winter_ready)
        ivWinterReady.setImageResource(
            if (currentInspectionData?.winterReady == true) R.drawable.ic_yes else R.drawable.ic_no
        )
        val tvHoneyHarvested = findViewById<TextView>(R.id.tv_honey_frames_harvested)
        val tvAggressivity = findViewById<TextView>(R.id.tv_aggressivity_value)
        val tvHiveName = findViewById<TextView>(R.id.tv_hive_name)
        val tvNotes = findViewById<TextView>(R.id.tv_notes)
        val tvFramesPerSuper = findViewById<TextView>(R.id.tv_frames_per_super)
        val tvSupersCount = findViewById<TextView>(R.id.tv_supers_count)
        val tvBroodFramesCount = findViewById<TextView>(R.id.tv_brood_frames_count)
        val tvHoneyFramesCount = findViewById<TextView>(R.id.tv_honey_frames_count)
        val tvDroneFramesCount = findViewById<TextView>(R.id.tv_drone_frames_count)
        val tvBroodChange = findViewById<TextView>(R.id.tv_brood_change)
        val tvHoneyChange = findViewById<TextView>(R.id.tv_honey_change)
        val tvDroneChange = findViewById<TextView>(R.id.tv_drone_change)

        val note = currentInspectionData?.let { NotesFunctionality.getNote(db, it.noteId) }
        tvNotes.text = note?.noteText ?: ""

        currentInspectionData?.let { data ->
            tvHoneyHarvested.text = data.honeyHarvested.toString()
            tvAggressivity.text = data.aggressivity.toString()
            tvHiveName.text = HivesFunctionality.getHiveNameById(db, data.hiveId)
            tvFramesPerSuper.text = data.framesPerSuper.toString()
            tvSupersCount.text = data.supers.toString()
            tvBroodFramesCount.text = data.broodFrames.toString()
            tvHoneyFramesCount.text = data.honeyFrames.toString()
            tvDroneFramesCount.text = data.droneBroodFrames.toString()
            tvBroodChange.text = data.broodFramesChange.toString()
            tvHoneyChange.text = data.honeyFramesChange.toString()
            tvDroneChange.text = data.droneBroodFramesChange.toString()
        }
    }
}