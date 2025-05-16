package com.example.brokskeeping.LogActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.HumTempDataFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding


class LogsBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var logsAdapter: LogsAdapter
    private var hiveId: Int = -1
    private var stationId: Int = -1
    private var stationName: String = ""
    private var hiveName: String = ""
    private lateinit var header: TextView
    private lateinit var btnLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)

        db = DatabaseHelper(this)
        logsAdapter = LogsAdapter(mutableListOf(), hiveId, db, this)

        //get stationName
        stationName = StationsFunctionality.getStationNameById(db, stationId)
        //get hiveName
        hiveName = HivesFunctionality.getHiveNameById(db, hiveId)

        // Set the text of the TextView to the stationName
        header = binding.tvCommonBrowserHeader
        header.text = "Logs of [$stationName], [$hiveName]"

        // buttons
        btnLayout = binding.llCommonBrowserButtonLayout
        val addLogButton = Button(this).apply {
            id = View.generateViewId()
            text = "Add Log"
            setTextColor(ContextCompat.getColor(this@LogsBrowserActivity, R.color.buttonTextColor))
            backgroundTintList = ContextCompat.getColorStateList(this@LogsBrowserActivity, R.color.buttonColor)
        }
        btnLayout.addView(addLogButton)

        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LogsBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = logsAdapter
        }

        // Set up the Floating Action Button (FAB) for adding a new station
        addLogButton.setOnClickListener {
            startAddLogActivity()
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