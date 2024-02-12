package com.example.brokskeeping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HumTempDataFunctionality
import com.example.brokskeeping.databinding.ActivityLogsBrowserBinding


class LogsBrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBrowserBinding
    private lateinit var db: DatabaseHelper
    private lateinit var logsAdapter: LogsAdapter
    private var hiveId: Int = -1
    private var stationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)

        db = DatabaseHelper(this)
        logsAdapter = LogsAdapter(mutableListOf(), hiveId, db, this)

        // Set the text of the TextView to the stationName
        binding.tvLogsLabel.text = "Station $stationId Hive $hiveId"

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LogsBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = logsAdapter
        }

        // Set up the Floating Action Button (FAB) for adding a new station
        binding.AddLogBt.setOnClickListener {
            startAddLogActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        val updatedLogsList = HumTempDataFunctionality.getAllHumTempData(db, hiveId)
        logsAdapter.updateData(updatedLogsList)
    }

    fun startAddLogActivity() {
        val intent = Intent(this, AddLogActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }

    fun startLogActivity(logId: Int) {
        val intent = Intent(this, LogActivity::class.java)
        intent.putExtra("logId", logId)
        startActivity(intent)
    }
}