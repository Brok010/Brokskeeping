package com.example.brokskeeping

import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.example.brokskeeping.DataClasses.HumTempData
import com.example.brokskeeping.DataClasses.SensorData
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HumTempDataFunctionality
import com.example.brokskeeping.DbFunctionality.Utils
import com.example.brokskeeping.databinding.ActivityLogBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

class LogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogBinding
    private var logId: Int = -1
    private lateinit var db: DatabaseHelper
    private lateinit var minTemp: TextView
    private lateinit var maxTemp: TextView
    private lateinit var minHum: TextView
    private lateinit var maxHum: TextView
    private lateinit var tvFirstDate: TextView
    private lateinit var tvLastDate: TextView
    private lateinit var btnReset: Button
    private lateinit var graphLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = DatabaseHelper(this)

        // Bind TextViews
        minTemp = findViewById(R.id.min_temp)
        maxTemp = findViewById(R.id.max_temp)
        minHum = findViewById(R.id.min_hum)
        maxHum = findViewById(R.id.max_hum)
        tvFirstDate = findViewById(R.id.first_date)
        tvLastDate = findViewById(R.id.last_date)
        graphLayout = findViewById(R.id.graphContainer)

        logId = intent.getIntExtra("logId", -1)

        val originalData = HumTempDataFunctionality.getHumTempData(db, logId)
        if (originalData.logText == "") {
            Toast.makeText(this, "Empty data", Toast.LENGTH_SHORT).show()
            finish()
        }

        val maxMins = Utils.getMaxMinTempHum(originalData.logText)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val formattedFirstDate = dateFormat.format(originalData.firstDate)
        val formattedLastDate = dateFormat.format(originalData.lastDate)

        //set 1st date and lastdate, put maxmins and avg graph in there
        minTemp.text = "Min temp: ${maxMins.minTemp}°C"
        maxTemp.text = "Max temp: ${maxMins.maxTemp}°C"
        minHum.text = "Min hum: ${maxMins.minHum}%"
        maxHum.text = "Max hum: ${maxMins.maxHum}%"
        tvFirstDate.text = formattedFirstDate
        tvLastDate.text = formattedLastDate
        btnReset = findViewById(R.id.btn_reset)
        graphLayout = findViewById(R.id.graphContainer)


        // for graph I don't need every value
        val originalCompressedData = Utils.compressData(originalData.logText, originalData.firstDate, originalData.lastDate)
//        makeGraph(originalCompressedData)

        // Initialize Calendar instances for the first and last dates
        val calFirstDate = Calendar.getInstance()
        calFirstDate.time = originalData.firstDate

        val calLastDate = Calendar.getInstance()
        calLastDate.time = originalData.lastDate

        var newFirstDate = ""
        var newLastDate = ""

        //adjust date
        tvFirstDate.setOnClickListener {
            newFirstDate = firstDatePicker(calFirstDate, dateFormat, originalData)
        }
        
        //adjust date
        tvLastDate.setOnClickListener {
            newLastDate = lastDatePicker(calLastDate, dateFormat, originalData)
        }

        // reset values
        btnReset.setOnClickListener {
            onReset(originalCompressedData, newFirstDate, newLastDate)
        }


        // Generate sample data
        val data = generateSampleData()

        // Make and display the graph
        makeGraph(data)
    }
    private fun generateSampleData(): List<Entry> {
        val entries = mutableListOf<Entry>()
        val random = Random()
        for (i in 0 until 10) {
            entries.add(Entry(i.toFloat(), random.nextFloat() * 100)) // Random values for demonstration
        }
        return entries
    }
    private fun makeGraph(data: List<Entry>) {
        val lineDataSet = LineDataSet(data, "Sample Data")
        lineDataSet.color = Color.BLUE
        lineDataSet.valueTextColor = Color.BLACK

        val lineData = LineData(lineDataSet)

        val lineChart = LineChart(this)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(false)

        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false

        graphLayout.addView(lineChart)
    }
    private fun onReset(data: MutableList<SensorData>, firstDate: String, lastDate: String) {
        // get new data and regenerate graph
//        val newData = data
//        makeGraph(newData)
    }

    private fun lastDatePicker(calLastDate: Calendar, dateFormat: SimpleDateFormat, originalData: HumTempData): String {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Update the Calendar instance with the selected date
                calLastDate.set(year, month, dayOfMonth)

                // Update the TextView with the formatted date
                val formattedDate = dateFormat.format(calLastDate.time)
                tvLastDate.text = formattedDate
            },
            calLastDate.get(Calendar.YEAR),
            calLastDate.get(Calendar.MONTH),
            calLastDate.get(Calendar.DAY_OF_MONTH)
        )

        // Set min date to originalData.firstDate
        datePickerDialog.datePicker.minDate = originalData.firstDate.time

        // Set max date to originalData.lastDate
        datePickerDialog.datePicker.maxDate = originalData.lastDate.time

        datePickerDialog.show()

        // Return the formatted date
        return dateFormat.format(calLastDate.time)
    }

    private fun firstDatePicker(calFirstDate: Calendar, dateFormat: SimpleDateFormat, originalData: HumTempData): String {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Update the Calendar instance with the selected date
                calFirstDate.set(year, month, dayOfMonth)

                // Update the TextView with the formatted date
                val formattedDate = dateFormat.format(calFirstDate.time)
                tvFirstDate.text = formattedDate
            },
            calFirstDate.get(Calendar.YEAR),
            calFirstDate.get(Calendar.MONTH),
            calFirstDate.get(Calendar.DAY_OF_MONTH)
        )

        // Set min date to originalData.firstDate
        datePickerDialog.datePicker.minDate = originalData.firstDate.time

        // Set max date to originalData.lastDate
        datePickerDialog.datePicker.maxDate = originalData.lastDate.time

        datePickerDialog.show()

        // Return the formatted date
        return dateFormat.format(calFirstDate.time)
    }
}