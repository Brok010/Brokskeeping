package com.example.brokskeeping.InspectionDataActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding

class InspectionDataBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var inspectionDataAdapter: InspectionDataAdapter
    private var inspectionId: Int = -1
    private var stationId: Int = -1
    private lateinit var header: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        inspectionId = intent.getIntExtra("inspectionId", -1)
        stationId = intent.getIntExtra("stationId", -1)
        val stationName = StationsFunctionality.getStationNameById(db, stationId)
        val (inspection, inspectionResult) = InspectionsFunctionality.getInspection(db, inspectionId)
        if (inspectionResult == 0) {
            Toast.makeText(this, "Could not retrieve inspection data", Toast.LENGTH_SHORT).show()
            finish()
        }
        val inspectionDate = inspection?.date
        inspectionDataAdapter = InspectionDataAdapter(mutableListOf(), inspectionId, db, this)

        // Bind views using ViewBinding
        header = binding.tvCommonBrowserHeader
        header.text = "Inspection of station ${stationName} on ${inspectionDate}"

        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@InspectionDataBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = inspectionDataAdapter
        }

        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(binding.commonFragmentContainer.id, fragment)
                .commit()
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

    fun startInspectionDataActivity(inspectionDataId: Int, inspectionId: Int) {
        val intent = Intent(this, InspectionDataActivity::class.java)
        intent.putExtra("inspectionDataId", inspectionDataId)
        intent.putExtra("inspectionId", inspectionId)
        startActivity(intent)
    }

    fun startAdjustInspectionDataActivity() {
    //
    }
}