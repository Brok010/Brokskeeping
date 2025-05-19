//TODO: add checkbox into view

package com.example.brokskeeping.ToDoActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brokskeeping.BottomMenuFragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.R
import com.example.brokskeeping.databinding.CommonBrowserRecyclerBinding

class ToDoBrowserActivity : AppCompatActivity() {
    private lateinit var binding: CommonBrowserRecyclerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var toDoAdapter: ToDoAdapter
    private var stationId: Int = -1
    private var hiveId: Int = -1
    private var state: Int = -1
    private var stationName: String = ""
    private var hiveName: String = ""
    private lateinit var header: TextView
    private lateinit var stateFilterInput: EditText
    private lateinit var stationFilterInput: EditText
    private lateinit var hiveFilterInput: EditText
    private lateinit var btnLayout: LinearLayout
    private lateinit var addToDoBt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommonBrowserRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)
        // Initialize the database helper and RecyclerView adapter
        db = DatabaseHelper(this)
        toDoAdapter = ToDoAdapter(mutableListOf(), db, this, hiveId)

        // Set up the RecyclerView
        binding.commonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ToDoBrowserActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = toDoAdapter
        }

        //get stationName
        stationName = StationsFunctionality.getStationNameById(db, stationId)
        //get hiveName
        hiveName = HivesFunctionality.getHiveNameById(db, hiveId)

        // Bind views using ViewBinding
        header = binding.tvCommonBrowserHeader

        // buttons
        btnLayout = binding.llCommonBrowserButtonLayout
        addToDoBt = Button(this).apply {
            id = View.generateViewId()
            text = "Add ToDo"
            setTextColor(ContextCompat.getColor(this@ToDoBrowserActivity, R.color.buttonTextColor))
            backgroundTintList = ContextCompat.getColorStateList(this@ToDoBrowserActivity, R.color.buttonColor)
        }
        btnLayout.addView(addToDoBt)

        // Set up the Floating Action Button (FAB) for adding a new station
        addToDoBt.setOnClickListener {
            startAddToDoActivity()
        }
        // bottom menu
        if (savedInstanceState == null) {
            val fragment = BottomMenuFragment()
            supportFragmentManager.beginTransaction()
                .replace(binding.commonFragmentContainer.id, fragment)
                .commit()
        }

        createAndAddStationHiveStateFilterLayout()

        stateFilterInput.setText("All")
        if (stationId < 1) {
            stationFilterInput.setText("All")
        }
        if (hiveId < 1) {
           hiveFilterInput.setText("All")
        }
    }

    private fun createAndAddStationHiveStateFilterLayout() {
        val filterLayout = binding.llFilters
        filterLayout.removeAllViews()

        // Colors
        val textColor = ContextCompat.getColor(this, R.color.basicTextColor)

        // Helper function to create filter item
        fun createFilter(labelText: String, hintText: String, editTextId: Int): LinearLayout {
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = 16
                }
            }

            val label = TextView(this).apply {
                text = labelText
                setTextColor(textColor)
            }

            val input = EditText(this).apply {
                id = editTextId
                hint = hintText
                isFocusable = false
                isClickable = true
                inputType = android.text.InputType.TYPE_NULL
                setPadding(16, 0, 16, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8
                    marginEnd = 8
                }
            }

            layout.addView(label)
            layout.addView(input)
            return layout
        }

        // Create individual filter views
        val stationLayout = createFilter("Station", "Station Filter", View.generateViewId())
        val hiveLayout = createFilter("Hive", "Hive Filter", View.generateViewId())
        val stateLayout = createFilter("State", "State Filter", View.generateViewId())

        // Save references if needed
        stationFilterInput = stationLayout.getChildAt(1) as EditText
        hiveFilterInput = hiveLayout.getChildAt(1) as EditText
        stateFilterInput = stateLayout.getChildAt(1) as EditText

        // Apply visibility logic based on stationId and hiveId
        if (stationId != -1 && hiveId != -1) {
            stationLayout.visibility = View.GONE
            hiveLayout.visibility = View.GONE
            binding.tvCommonBrowserHeader.text = "To Do's of hive [$hiveName]; [$stationName]"
        } else {
            binding.tvCommonBrowserHeader.text = "To Do's"
        }

        // Add to container
        filterLayout.addView(stationLayout)
        filterLayout.addView(hiveLayout)
        filterLayout.addView(stateLayout)

        // Set listeners
        stationFilterInput.setOnClickListener { stationFilter() }
        hiveFilterInput.setOnClickListener { hiveFilter() }
        stateFilterInput.setOnClickListener { stateFilter() }
    }


    override fun onResume() {
        super.onResume()

        // Show or hide Add button based on selection
        if (stationId > 0 && hiveId > 0) {
            addToDoBt.visibility = View.VISIBLE
        } else {
            addToDoBt.visibility = View.GONE
        }

        val (updatedToDoList, result) = ToDoFunctionality.getAllToDos(db, hiveId, stationId, state, 0)
        if (result == 1) {
            toDoAdapter.updateData(updatedToDoList)
        } else {
            Toast.makeText(this, "ToDoUpdate didn't finish successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stationFilter() {
        val (stations, result) = StationsFunctionality.getAllStations(db, 1)
        if (result == 0) {
            Log.e("ToDoBrowserActivity", "Station loading was not successful - stationFilter")
        }

        val stationNames = mutableListOf("All")
        val stationIds = mutableListOf(0)

        stations.forEach {
            stationNames.add(StationsFunctionality.getStationNameById(db, it.id))
            stationIds.add(it.id)
        }

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Choose a station")
        builder.setItems(stationNames.toTypedArray()) { _, which ->
            stationId = stationIds[which]
            hiveId = 0 // Reset to All since station changed
            stationFilterInput.setText(stationNames[which])
           hiveFilterInput.setText("All")
            onResume()
        }
        builder.show()
    }

    private fun hiveFilter() {
        if (stationId < 1) {
            Toast.makeText(this, "Please choose a station first.", Toast.LENGTH_SHORT).show()
        } else {
            val (hives, result) = HivesFunctionality.getAllHives(db, stationId, 0)
            if (result != 1 || hives.isEmpty()) {
                Toast.makeText(this, "No hives found for this station.", Toast.LENGTH_SHORT).show()
            } else {
                val hiveNames = mutableListOf("All")
                val hiveIds = mutableListOf(0)

                hives.forEach {
                    hiveNames.add(HivesFunctionality.getHiveNameById(db, it.id))
                    hiveIds.add(it.id)
                }

                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Choose a hive")
                builder.setItems(hiveNames.toTypedArray()) { _, which ->
                    hiveId = hiveIds[which]
                    hiveFilterInput.setText(hiveNames[which])
                    onResume()
                }
                builder.show()
            }
        }
    }

    fun stateFilter() {
        val options = arrayOf("All", "Done", "To be done")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Choose State")
        builder.setItems(options) { _, which ->
            state = when (which) {
                0 -> -1 // All
                1 -> 1  // Done
                2 -> 0  // To Be Done
                else -> -1
            }
            stateFilterInput.setText(options[which])
            onResume()
        }
        builder.show()
    }

    fun startAddToDoActivity() {
        val intent = Intent(this, AddToDoActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }
    fun startAdjustToDoActivity(toDoId: Int) {
        val intent = Intent(this, AdjustToDoActivity::class.java)
        intent.putExtra("toDoId", toDoId)
        startActivity(intent)
    }

    fun startChangeStateToDoActivity(toDoId: Int) {
        ToDoFunctionality.toggleToDoState(db, toDoId)
        onResume()
    }
}