//TODO: add functionality that would allow user to choose the line he wants to inspect
package com.example.brokskeeping.LogActivities

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.brokskeeping.DataClasses.HumTempData
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HumTempDataFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.ActivityLogBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    @SuppressLint("SetTextI18n")
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
        var cutOriginalData = ""
        if (originalData.logText == "") {
            Toast.makeText(this, getString(R.string.empty_data), Toast.LENGTH_SHORT).show()
            finish()
        } else if (originalData.logText.isNotEmpty()) {
            cutOriginalData = originalData.logText.substringAfter("\n", "")
        }

        val maxMins = Utils.getMaxMinTempHum(cutOriginalData)

        // if the difference between first and last date is less then 24h the format is then HH:mm:ss
        var moreThanDayFlag = Utils.isMoreThanDay(originalData.firstDate, originalData.lastDate)

        var dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        if (moreThanDayFlag) {
            dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        }
        val formattedFirstDate = dateFormat.format(originalData.firstDate)
        val formattedLastDate = dateFormat.format(originalData.lastDate)

        //set 1st date and lastdate, put maxmins and avg graph in there
        minTemp.text = getString(R.string.min_temp_c, maxMins.minTemp)
        maxTemp.text = getString(R.string.max_temp_c, maxMins.maxTemp)
        minHum.text = getString(R.string.min_hum, maxMins.minHum)
        maxHum.text = getString(R.string.max_hum, maxMins.maxHum)
        tvFirstDate.text = formattedFirstDate
        tvLastDate.text = formattedLastDate
        btnReset = findViewById(R.id.btn_reset)
        graphLayout = findViewById(R.id.graphContainer)

        makeGraph(cutOriginalData)

        // Initialize Calendar instances for the first and last dates
        val calFirstDate = Calendar.getInstance()
        calFirstDate.time = originalData.firstDate

        val calLastDate = Calendar.getInstance()
        calLastDate.time = originalData.lastDate

        //adjust date
        tvFirstDate.setOnClickListener {
            datePicker(calFirstDate, dateFormat, originalData, tvFirstDate)
        }

        //adjust date
        tvLastDate.setOnClickListener {
            datePicker(calLastDate, dateFormat, originalData, tvLastDate)
        }

        // reset values
        btnReset.setOnClickListener {
            onReset(originalData.logText)
        }
    }

    //TODO: these functions should be in separate file
    private fun makeGraph(data: String) {

        val tempEntries = mutableListOf<Entry>()
        val humEntries = mutableListOf<Entry>()

        parseData(data, tempEntries, humEntries)

        //avg the entries
        val avgTempEntries = averageEntries(tempEntries)
        val avgHumEntries = averageEntries(humEntries)

        val tempDataSet = createTempDataSet(avgTempEntries)
        val humDataSet = createHumDataSet(avgHumEntries)

        val lineData = LineData(tempDataSet, humDataSet)

        val lineChart = LineChart(this)
        lineChart.data = lineData

        configureXAxis(lineChart)
        configureYAxis(lineChart)

        graphLayout.removeAllViews()
        graphLayout.addView(lineChart)
    }

    private fun averageEntries(entries: MutableList<Entry>): MutableList<Entry> {
        val sortedEntries = entries.sortedBy { it.x } // Sort entries by x-value
        val avgEntries = mutableListOf<Entry>()
        val groupedEntries = sortedEntries.groupBy { it.x } // Group sorted entries by x-axis value

        for ((xValue, group) in groupedEntries) {
            // Calculate average y value for each x-axis value
            val sum = group.sumOf { it.y.toDouble() }
            val average = sum / group.size.toFloat()
            avgEntries.add(Entry(xValue, average.toFloat()))
        }
        return avgEntries
    }

    private fun parseData(data: String, tempEntries: MutableList<Entry>, humEntries: MutableList<Entry>) {
        val lines = data.split("\n")
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()

        lines.forEach { line ->
            val parts = line.split(", ")
            if (parts.size >= 3) {
                val timeString = parts[0]
                val tempString = parts[1]
                val humString = parts[2]

                val time = dateFormat.parse(timeString)
                calendar.time = time
                val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val hourFraction = hourOfDay + minute / 60f // Combine hour and minute into a fraction

                tempEntries.add(Entry(hourFraction, tempString.toFloat()))
                humEntries.add(Entry(hourFraction, humString.toFloat()))
            }
        }
    }

    private fun createTempDataSet(tempEntries: MutableList<Entry>): LineDataSet {
        val tempDataSet = LineDataSet(tempEntries, getString(R.string.temperature_c))
        tempDataSet.color = Color.BLUE
        tempDataSet.setCircleColor(Color.BLUE)
        tempDataSet.setDrawValues(false) // Disable drawing values on points
        return tempDataSet
    }

    private fun createHumDataSet(humEntries: MutableList<Entry>): LineDataSet {
        val humDataSet = LineDataSet(humEntries, getString(R.string.humidity))
        humDataSet.color = Color.RED
        humDataSet.setCircleColor(Color.RED)
        humDataSet.setDrawValues(false) // Disable drawing values on points
        return humDataSet
    }

    private fun configureXAxis(lineChart: LineChart) {
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Set the granularity to 1 to show all values
    }



    private fun configureYAxis(lineChart: LineChart) {
        val yAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(false)
        lineChart.axisRight.isEnabled = false
    }

    private fun onReset(data: String) {
        val firstDate = tvFirstDate.text.toString()
        val lastDate = tvLastDate.text.toString()
        if (firstDate.isEmpty() || lastDate.isEmpty()) {
            Toast.makeText(this,
                getString(R.string.please_select_both_first_and_last_dates), Toast.LENGTH_SHORT).show()
        } else {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

            try {
                val firstDateTime = dateFormat.parse(firstDate)?.time ?: 0L
                val lastDateTime = dateFormat.parse(lastDate)?.time ?: 0L

                if (firstDateTime > lastDateTime) {
                    Toast.makeText(this,
                        getString(R.string.first_date_cannot_be_after_the_last_date), Toast.LENGTH_SHORT).show()
                } else {
                    val newData = Utils.getNewData(data, firstDateTime, lastDateTime)
                    makeGraph(newData)
                }
            } catch (e: ParseException) {
                Toast.makeText(this, getString(R.string.error_parsing_dates), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun datePicker(calLastDate: Calendar, dateFormat: SimpleDateFormat, originalData: HumTempData, textViewToUpdate: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.time_picker_dialog, null)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.date_picker)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hour_picker)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minute_picker)

        datePicker.init(
            calLastDate.get(Calendar.YEAR),
            calLastDate.get(Calendar.MONTH),
            calLastDate.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            // Update the Calendar instance with the selected date
            calLastDate.set(year, monthOfYear, dayOfMonth)
        }

        // Set minimum and maximum dates for the date picker
        datePicker.minDate = originalData.firstDate.time
        datePicker.maxDate = originalData.lastDate.time

        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.value = calLastDate.get(Calendar.HOUR_OF_DAY)

        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.value = calLastDate.get(Calendar.MINUTE)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                // Update the TextView with the formatted date and time
                calLastDate.set(Calendar.HOUR_OF_DAY, hourPicker.value)
                calLastDate.set(Calendar.MINUTE, minutePicker.value)
                val formattedDate = dateFormat.format(calLastDate.time)
                textViewToUpdate.text = formattedDate
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }
}