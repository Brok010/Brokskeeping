package com.example.brokskeeping.InspectionDataActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.databinding.ActivityInspectionDataBrowserBinding

class InspectionDataBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInspectionDataBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var inspectionDataAdapter: InspectionDataAdapter
    private var inspectionId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInspectionDataBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        inspectionId = intent.getIntExtra("inspectionId", -1)

        inspectionDataAdapter = InspectionDataAdapter(mutableListOf(), inspectionId, db, this)

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InspectionDataBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = inspectionDataAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        val (updatedInspectionDataList, result) = InspectionsFunctionality.getAllInspectionDataForInspectionId(db, inspectionId)
        if (result == 1) {
            inspectionDataAdapter.updateData(updatedInspectionDataList)
        } else {
            Toast.makeText(this, "Cannot load inspection data", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun startInspectionDataActivity(inspectionDataId: Int) {
        val intent = Intent(this, InspectionDataActivity::class.java)
        intent.putExtra("inspectionDataId", inspectionDataId)
        startActivity(intent)
    }

    fun startAdjustInspectionDataActivity() {
    //
    }
}