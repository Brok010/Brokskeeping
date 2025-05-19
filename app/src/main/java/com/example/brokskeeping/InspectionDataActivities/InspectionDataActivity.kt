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
            Toast.makeText(this, "InspectionDataLoad not successful", Toast.LENGTH_SHORT).show()
            finish()
        }

        // date
        val (inspection, inspectionResult) = InspectionsFunctionality.getInspection(db, currentInspectionData!!.inspectionId)
        if (inspectionResult == 0) {
            Toast.makeText(this, "Inspection load not successful", Toast.LENGTH_SHORT).show()
        }
        val tvDate = findViewById<TextView>(R.id.tv_date)
        if (inspection != null) {
            tvDate.text = inspection.date.toString()
        } else {
            Toast.makeText(this, "Inspection has no date", Toast.LENGTH_SHORT).show()
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
            if (currentInspectionData?.supplementedFeed!! > 0.0) R.drawable.ic_yes else R.drawable.ic_no
        )
        val ivWinterReady = findViewById<ImageView>(R.id.iv_winter_ready)
        ivWinterReady.setImageResource(
            if (currentInspectionData?.winterReady == true) R.drawable.ic_yes else R.drawable.ic_no
        )
        val ivSeparated = findViewById<ImageView>(R.id.iv_separated)
        ivSeparated.setImageResource(
            if (currentInspectionData?.separated != -1) R.drawable.ic_yes else R.drawable.ic_no
        )
        val ivJoined = findViewById<ImageView>(R.id.iv_joined)
        ivJoined.setImageResource(
            if (currentInspectionData?.joined != -1) R.drawable.ic_yes else R.drawable.ic_no
        )
        val tvHoneyHarvested = findViewById<TextView>(R.id.tv_honey_frames_harvested)
        val tvAggressivity = findViewById<TextView>(R.id.tv_aggressivity)
        val tvAttention = findViewById<TextView>(R.id.tv_attention)
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
        val tvColonyEndState = findViewById<TextView>(R.id.tv_colony_end_state)
        val tvSeparated = findViewById<TextView>(R.id.tv_separated_count)
        val tvJoined = findViewById<TextView>(R.id.tv_joined_count)

        val note = currentInspectionData?.let { NotesFunctionality.getNote(db, it.noteId) }
        tvNotes.text = note?.noteText ?: ""

        val colonyEndState = if (currentInspectionData?.colonyEndState == 0) {
            "Dead"
        } else if (currentInspectionData?.colonyEndState == -1) {
            "Alive"
        } else {
            currentInspectionData?.colonyEndState.toString()
        }

        val separatedValue = if (currentInspectionData?.separated == -1) {
            ""
        } else {
            currentInspectionData?.separated.toString()
        }

        val joinedValue = if (currentInspectionData?.joined == -1) {
            ""
        } else {
            currentInspectionData?.joined.toString()
        }

        currentInspectionData?.let { data ->
            tvHoneyHarvested.text = data.honeyHarvested.toString()
            tvAggressivity.text = data.aggressivity.toString()
            tvAttention.text = data.attentionWorth.toString()
            tvHiveName.text = HivesFunctionality.getHiveNameById(db, data.hiveId)
            tvFramesPerSuper.text = data.framesPerSuper.toString()
            tvSupersCount.text = data.supers.toString()
            tvBroodFramesCount.text = data.broodFrames.toString()
            tvHoneyFramesCount.text = data.honeyFrames.toString()
            tvDroneFramesCount.text = data.droneBroodFrames.toString()
            tvBroodChange.text = data.broodFramesChange.toString()
            tvHoneyChange.text = data.honeyFramesChange.toString()
            tvDroneChange.text = data.droneBroodFramesChange.toString()
            tvColonyEndState.text = colonyEndState
            tvSeparated.text = separatedValue
            tvJoined.text = joinedValue
        }
    }
}