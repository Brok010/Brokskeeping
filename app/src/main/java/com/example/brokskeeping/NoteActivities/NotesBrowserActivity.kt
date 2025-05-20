package com.example.brokskeeping.NoteActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BarcodeScan
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.R
import com.example.brokskeeping.ToDoActivities.ToDoActivity
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding
import java.util.Calendar


class NotesBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private var hiveId: Int = -1
    private var stationId: Int = -1
    private var qrString: String = ""
    private var stationName: String = ""
    private var hiveName: String = ""
    private var qrHiveId = -1
    private var selectedYear: Int? = null
    private var selectedMonth: Int? = null
    private lateinit var header: TextView
    private lateinit var btnLayout: LinearLayout
    private lateinit var dateFilterInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)
        qrString = intent.getStringExtra("qrString") ?: ""

        db = DatabaseHelper(this)

        // means we are getting here from a qr scanner and hiveId is the hiveId we are getting here from
        if (qrString != "") {
            qrHiveId = HivesFunctionality.getHiveIdByQRString(db, qrString)
            if (qrHiveId == -1) {
                // hive not found
                Toast.makeText(this, getString(R.string.hive_not_found), Toast.LENGTH_SHORT).show()
                val intent = Intent(this, BarcodeScan::class.java)
                Handler(Looper.getMainLooper()).postDelayed({   // TODO check
                    startActivity(intent)
                    finish()
                }, 2000)
                startActivity(intent)
                finish()
                // TODO if user presses back it puts him into an empty notes activity
            } else if (hiveId == qrHiveId){
                // we are on the correct hive
                Toast.makeText(this,
                    getString(R.string.u_are_on_the_correct_hive), Toast.LENGTH_SHORT).show()
                stationId = HivesFunctionality.getStationIdByHiveId(db, qrHiveId)
            } else {
                //continue in routing to the next hive
                stationId = HivesFunctionality.getStationIdByHiveId(db, qrHiveId)
                // i need to load the correct hive
                hiveId = qrHiveId
            }
        }


        notesAdapter = NotesAdapter(mutableListOf(), hiveId, db, this)

        //get stationName
        stationName = StationsFunctionality.getStationNameById(db, stationId)
        //get hiveName
        hiveName = HivesFunctionality.getHiveNameById(db, hiveId)

        // Set the text of the TextView to the stationName
        header = binding.tvCommonBrowserHeader
        header.text = getString(R.string.notes_of_s_s, stationName, hiveName)

        // buttons
        btnLayout = binding.llCommonBrowserButtonLayout
        val addNoteButton = Button(this).apply {
            id = View.generateViewId()
            text = context.getString(R.string.add_note)
            setTextColor(ContextCompat.getColor(this@NotesBrowserActivity, R.color.buttonTextColor))
            backgroundTintList = ContextCompat.getColorStateList(this@NotesBrowserActivity, R.color.buttonColor)
        }
        btnLayout.addView(addNoteButton)

        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotesBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = notesAdapter
        }

        // bottom menu
        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            val bundle = Bundle()
            bundle.putInt("hiveId", hiveId)  // Replace `yourHiveId` with the actual hive ID you want to pass
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(binding.commonFragmentContainer.id, fragment)
                .commit()
        }

        //Add button
        addNoteButton.setOnClickListener {
            startAddNoteActivity()
        }
        createAndAddFilterLayout()

    }

    override fun onResume() {
        super.onResume()
        val (updatedNotesList, result) = NotesFunctionality.getAllNotes(db, hiveId, selectedYear, selectedMonth, true)
        if (result == 0) {
            Toast.makeText(this, getString(R.string.couldn_t_retrieve_notes), Toast.LENGTH_SHORT).show()
        }
        notesAdapter.updateData(updatedNotesList)
    }

    fun createAndAddFilterLayout() {
        val filterLayout = binding.llFilters
        filterLayout.removeAllViews()

        // Date filter layout
        val dateLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 16
            }
        }

        val dateLabel = TextView(this).apply {
            text = getString(R.string.date)
            setTextColor(ContextCompat.getColor(this@NotesBrowserActivity, R.color.basicTextColor))
        }

        dateFilterInput = EditText(this).apply {
            id = View.generateViewId()
            hint = context.getString(R.string.date_filter)
            isFocusable = false
            isClickable = true
            inputType = InputType.TYPE_NULL
            setPadding(16, 0, 16, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 8
                marginEnd = 8
            }
        }

        dateLayout.addView(dateLabel)
        dateLayout.addView(dateFilterInput)

        // Add both to root filter layout
        filterLayout.addView(dateLayout)

        dateFilterInput.setOnClickListener {
            showTimeFilterDialog()
        }
    }

    private fun showTimeFilterDialog() {
        val options = arrayOf(getString(R.string.month), getString(R.string.year), getString(R.string.all_time))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_time_filter))
        builder.setItems(options) { _, which ->
            when (options[which]) {
                getString(R.string.month) -> {
                    showMonthYearPicker()
                }
                getString(R.string.year) -> {
                    showYearPicker()
                }
                getString(R.string.all_time) -> {
                    dateFilterInput.setText(getString(R.string.all_time))
                    selectedYear = null
                    selectedMonth = null
                    onResume()
                }
            }
        }
        builder.show()
    }

    private fun showYearPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 10..currentYear + 1).map { it.toString() }.toTypedArray()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val yearPicker = Spinner(this)
        yearPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        layout.addView(TextView(this).apply { text = getString(R.string.select_year) })
        layout.addView(yearPicker)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_year))
            .setView(layout)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                selectedYear = yearPicker.selectedItem.toString().toIntOrNull()
                selectedMonth = null
                dateFilterInput.setText("${selectedYear.toString()}")
                onResume()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showMonthYearPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 10..currentYear + 1).map { it.toString() }.toTypedArray()
        val months = (1..12).map { it.toString().padStart(2, '0') }.toTypedArray()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val yearPicker = Spinner(this)
        yearPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        layout.addView(TextView(this).apply { text = getString(R.string.select_year) })
        layout.addView(yearPicker)

        val monthPicker = Spinner(this)
        monthPicker.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        layout.addView(TextView(this).apply { text = getString(R.string.select_month) })
        layout.addView(monthPicker)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_month_and_year))
            .setView(layout)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                selectedYear = yearPicker.selectedItem.toString().toIntOrNull()
                selectedMonth = monthPicker.selectedItem.toString().toIntOrNull()
                dateFilterInput.setText("${selectedMonth.toString()}/${selectedYear.toString()}")
                onResume()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }


    fun startToDoActivity() {
        val intent = Intent(this, ToDoActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }

    fun startNoteActivity(noteId: Int) {
        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("noteId", noteId)
        startActivity(intent)
    }

    fun startAdjustNotesActivity(noteId: Int) {
        val intent = Intent(this, AdjustNoteActivity::class.java)
        intent.putExtra("stationId", stationId)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("noteId", noteId)
        startActivity(intent)
    }

    fun startAddNoteActivity() {
        val intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        intent.putExtra("stationId", stationId)
        startActivity(intent)
    }
}