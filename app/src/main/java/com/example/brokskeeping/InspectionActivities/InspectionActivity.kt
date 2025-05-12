package com.example.brokskeeping.InspectionActivities

import android.content.Intent
import com.example.brokskeeping.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DataClasses.HiveNotes
import com.example.brokskeeping.DataClasses.HoneyHarvest
import com.example.brokskeeping.DataClasses.Inspection
import com.example.brokskeeping.DataClasses.InspectionData
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.DbFunctionality.HoneyHarvestFunctionality
import com.example.brokskeeping.DbFunctionality.InspectionsFunctionality
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.ToDoActivities.AddToDoActivity
import com.example.brokskeeping.ToDoActivities.ToDoActivity
import com.example.brokskeeping.databinding.ActivityInspectionContainerBinding
import com.google.android.material.slider.Slider
import java.util.Date
import kotlin.Int


class InspectionActivity : AppCompatActivity() {
    private var inspectionContainer: LinearLayout? = null
    private lateinit var binding: ActivityInspectionContainerBinding
    private val inspectionData: MutableList<View> = ArrayList() // Track inflated views
    private var beehiveData: List<Beehive>? = null
    private lateinit var db: DatabaseHelper
    private var stationId: Int = -1
    private lateinit var toDoLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private var currentInspectionIndex: Int = 0
    private var lastToDoHiveIndex: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInspectionContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stationId = intent.getIntExtra("stationId", -1)
        db = DatabaseHelper(this)

        inspectionContainer = binding.inspectionContainer
        val (data, result) = HivesFunctionality.getAllHives(db, stationId)
        if (result == 0 || data.isEmpty()) {
            Toast.makeText(this, "Could not load hives", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        beehiveData = data // assign to the class property

        beehiveData!!.forEachIndexed { index, hive ->
            addInspectionView(hive, index)
        }
        showInspection(0) // Show first hive view only

        toDoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val currentView = inspectionData[lastToDoHiveIndex]
                val checkboxToDo = currentView.findViewById<CheckBox>(R.id.checkbox_added_to_do)

                checkboxToDo?.isChecked = true
            }
        }
    }

    private fun addInspectionView(hive: Beehive, index: Int) {
        val inflater = LayoutInflater.from(this)
        val inspectionView = inflater.inflate(R.layout.view_inspection_content, inspectionContainer, false)

        // Fill in hive-specific data
        inspectionView.findViewById<TextView>(R.id.tv_hive_name).text = "Hive: ${hive.nameTag}"

        val spinner = inspectionView.findViewById<Spinner>(R.id.spinner_frames_per_super)
        val framesPerSuperOptions = (1..18).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, framesPerSuperOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        if (hive.framesPerSuper >= 1) {
            spinner.setSelection(hive.framesPerSuper - 1)
        } else {
            spinner.setSelection(12)
        }

        // supers, Handle + / - buttons
        val btnAddSuper = inspectionView.findViewById<Button>(R.id.btn_add_super)
        val btnRemoveSuper = inspectionView.findViewById<Button>(R.id.btn_remove_super)
        val tvSuperCount = inspectionView.findViewById<TextView>(R.id.tv_super_count)

        if (hive.supers >= 1)  {
            tvSuperCount.text = hive.supers.toString()
        } else {
            tvSuperCount.text = 1.toString()
        }

        btnAddSuper.setOnClickListener {
            val count = tvSuperCount.text.toString().toInt()
            tvSuperCount.text = (count + 1).toString()
        }
        btnRemoveSuper.setOnClickListener {
            val count = tvSuperCount.text.toString().toInt()
            if (count > 0) tvSuperCount.text = (count - 1).toString()
        }

        // frames setting
        val broodFramesEditText = inspectionView.findViewById<EditText>(R.id.edittext_brood_frames)
        if (hive.broodFrames >= 1) {
            broodFramesEditText.setText(hive.broodFrames.toString())
        } else {
            broodFramesEditText.setText("")
        }

        val honeyFramesEditText = inspectionView.findViewById<EditText>(R.id.edittext_honey_frames)
        if (hive.honeyFrames >= 1) {
            honeyFramesEditText.setText(hive.honeyFrames.toString())
        } else {
            honeyFramesEditText.setText("")
        }

        val droneFramesEditText = inspectionView.findViewById<EditText>(R.id.edittext_drone_frames)
        if (hive.droneBroodFrames >= 1) {
            droneFramesEditText.setText(hive.droneBroodFrames.toString())
        } else {
            droneFramesEditText.setText("")
        }

        // Adjust checkboxes
        val broodAdjustedCheckbox = inspectionView.findViewById<CheckBox>(R.id.checkbox_brood_taken)
        broodAdjustedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                askAdjustment("Brood frames") { result ->
                    // Save the change to the tag
                    broodAdjustedCheckbox.tag = result
                }
            } else {
                broodAdjustedCheckbox.tag = 0 // Reset if unchecked
            }
        }

        val honeyAdjustedCheckbox = inspectionView.findViewById<CheckBox>(R.id.checkbox_honey_taken)
        honeyAdjustedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                askAdjustment("Honey frames") { result ->
                    honeyAdjustedCheckbox.tag = result
                }
            } else {
                honeyAdjustedCheckbox.tag = 0
            }
        }

        val droneAdjustedCheckbox = inspectionView.findViewById<CheckBox>(R.id.checkbox_drone_taken)
        droneAdjustedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                askAdjustment("Drone frames") { result ->
                    droneAdjustedCheckbox.tag = result
                }
            } else {
                droneAdjustedCheckbox.tag = 0
            }
        }

        // honey harvest
        val honeyHarvested = inspectionView.findViewById<TextView>(R.id.honey_frames_harvested)
        honeyHarvested.text = ""

        // slider
        val slider = inspectionView.findViewById<Slider>(R.id.aggressivity_slider)
        val initialValue = if (hive.aggressivity < 1 || hive.aggressivity > 5) 1 else hive.aggressivity
        slider.value = initialValue.toFloat()

        // Dynamic buttons
        val btnLayout = inspectionView.findViewById<LinearLayout>(R.id.btn_layout)
        btnLayout.removeAllViews()

        if (index > 0) {
            btnLayout.addView(createNavButton("Previous") {
                showInspection(index - 1)
            })
        }

        if (index < beehiveData!!.size - 1) {
            btnLayout.addView(createNavButton("Next") {
                showInspection(index + 1)
            })
        } else {
            btnLayout.addView(createNavButton("Save All") {
                saveAllToDatabase()
            })
        }

        btnLayout.addView(createNavButton("Pause") {
            //
        })

        // ToD0's
        val btnAddToDo = inspectionView.findViewById<Button>(R.id.bt_add_todo)
        val (getPendingToDos, _) = ToDoFunctionality.getAllToDos(db, hive.id, 0, 0, 1)
        btnAddToDo.text = "Pending todo's (${getPendingToDos.size})"

        btnAddToDo.setOnClickListener {
            if (getPendingToDos.isNotEmpty()) {
                val options = arrayOf("Add new to-do", "Show pending to-dos")
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("Choose an option for to-do")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> startAddToDoActivity(hive.id, index)
                            1 -> startToDoActivity(hive.id)
                        }
                    }
                builder.show()
            } else {
                startAddToDoActivity(hive.id, index)
            }
        }

        // winter ready
        val winterReadyCheckbox = inspectionView.findViewById<CheckBox>(R.id.checkbox_winter_ready)
        winterReadyCheckbox.isChecked = hive.winterReady

        inspectionView.visibility = View.GONE // Hidden until selected
        inspectionData.add(inspectionView)
        inspectionContainer!!.addView(inspectionView)
    }

    private fun askAdjustment(frameType: String, onResult: (Int) -> Unit) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("$frameType Adjusted")
        builder.setMessage("Did you add or remove $frameType?")

        builder.setPositiveButton("Added") { _, _ ->
            askHowMany { amount -> onResult(amount) } // Positive
        }

        builder.setNegativeButton("Removed") { _, _ ->
            askHowMany { amount -> onResult(-amount) } // Negative
        }

        builder.setNeutralButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun askHowMany(onAmountEntered: (Int) -> Unit) {
        val input = EditText(this).apply {
            hint = "Enter number of frames"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            addView(TextView(this@InspectionActivity).apply {
                text = "How many?"
                setPadding(0, 0, 0, 10)
            })
            addView(input)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Specify Amount")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val amount = input.text.toString().toIntOrNull() ?: 0
                onAmountEntered(amount)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }


    private fun saveAllToDatabase() {
        val insertedInspectionIds = mutableListOf<Int>()

        inspectionData.forEachIndexed { index, view ->
            val hive = beehiveData!![index]

            val broodFramesEditText = view.findViewById<EditText>(R.id.edittext_brood_frames)
            val honeyFramesEditText = view.findViewById<EditText>(R.id.edittext_honey_frames)
            val droneFramesEditText = view.findViewById<EditText>(R.id.edittext_drone_frames)

            val broodAdjusted = view.findViewById<CheckBox>(R.id.checkbox_brood_taken).isChecked
            val honeyAdjusted = view.findViewById<CheckBox>(R.id.checkbox_honey_taken).isChecked
            val droneAdjusted = view.findViewById<CheckBox>(R.id.checkbox_drone_taken).isChecked

            val spinner = view.findViewById<Spinner>(R.id.spinner_frames_per_super)
            val framesPerSuper = spinner.selectedItem.toString().toIntOrNull() ?: -1

            val tvSuperCount = view.findViewById<TextView>(R.id.tv_super_count)
            val superCount = tvSuperCount.text.toString().toIntOrNull() ?: -1

            val supplementedFeedCheckbox = view.findViewById<CheckBox>(R.id.checkbox_supplemented_feed)
            val winterReadyCheckbox = view.findViewById<CheckBox>(R.id.checkbox_winter_ready)

            val broodFrames = broodFramesEditText.text.toString().toIntOrNull() ?: -1
            val honeyFrames = honeyFramesEditText.text.toString().toIntOrNull() ?: -1
            val droneFrames = droneFramesEditText.text.toString().toIntOrNull() ?: -1

            // For change detection (adjustments), assume changes were saved to a tag on the checkbox
            val broodChange = view.findViewById<CheckBox>(R.id.checkbox_brood_taken).tag as? Int ?: -1
            val honeyChange = view.findViewById<CheckBox>(R.id.checkbox_honey_taken).tag as? Int ?: -1
            val droneChange = view.findViewById<CheckBox>(R.id.checkbox_drone_taken).tag as? Int ?: -1
            val honeyHarvested = view.findViewById<EditText>(R.id.honey_frames_harvested).text.toString().toIntOrNull() ?: -1

            val freeSpaceFrames = (superCount * framesPerSuper + honeyHarvested) - (broodFrames + honeyFrames + droneFrames)
            val notesEditText = view.findViewById<EditText>(R.id.edittext_notes)
            val notes = notesEditText.text.toString()

            val hiveNotes = HiveNotes(
                id = -1,
                stationId = stationId,
                hiveId = hive.id,
                noteText = notes,
                noteDate = Date()
            )

            val (noteId, result) = NotesFunctionality.addNote(db, hiveNotes)
            if (result != 1) {
                Toast.makeText(this, "addNote function crashed", Toast.LENGTH_SHORT).show()
                finish()
            }

            val aggressivity = view.findViewById<Slider>(R.id.aggressivity_slider).value.toInt()


            val inspectionData = InspectionData(
                id = -1, // to be auto-generated by DB
                hiveId = hive.id,
                noteId = noteId,
                broodFrames = broodFrames,
                broodFramesAdjusted = broodAdjusted,
                broodFramesChange = broodChange,
                honeyFrames = honeyFrames,
                honeyFramesAdjusted = honeyAdjusted,
                honeyFramesChange = honeyChange,
                droneBroodFrames = droneFrames,
                droneBroodFramesAdjusted = droneAdjusted,
                droneBroodFramesChange = droneChange,
                supers = superCount,
                framesPerSuper = framesPerSuper,
                freeSpaceFrames = freeSpaceFrames,
                supplementedFeed = supplementedFeedCheckbox.isChecked,
                winterReady = winterReadyCheckbox.isChecked,
                aggressivity = aggressivity,
                honeyHarvested = honeyHarvested
            )

            var hivesSupplementedFeedCount = 0
            if (supplementedFeedCheckbox.isChecked) {
                hivesSupplementedFeedCount = hive.supplementedFeedCount + 1
            }

            val newSupplementedFeedCount = hive.supplementedFeedCount + hivesSupplementedFeedCount
            val beehive = Beehive(
                id = hive.id,
                broodFrames = broodFrames,
                honeyFrames = honeyFrames,
                framesPerSuper = framesPerSuper,
                supers = superCount,
                droneBroodFrames = droneFrames,
                freeSpaceFrames = freeSpaceFrames,
                supplementedFeedCount = newSupplementedFeedCount,
                winterReady = winterReadyCheckbox.isChecked,
                aggressivity = aggressivity,
            )

            val (insertedId, _) = InspectionsFunctionality.saveInspectionData(db, inspectionData)
            insertedInspectionIds.add(insertedId)
            HivesFunctionality.updateHive(db, beehive)

            val honeyHarvest = HoneyHarvest(
                id = -1,
                hiveId = hive.id,
                date = Date(),
                honeyFrames = honeyHarvested,
            )
            HoneyHarvestFunctionality.saveHoneyHarvest(db, honeyHarvest)
        }

        val inspection = Inspection(
            stationId = stationId,
            finished = true,
            date = Date(),
            inspectionDataIds = insertedInspectionIds
        )
        InspectionsFunctionality.saveInspection(db, inspection)
        finish()
    }

    private fun createNavButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setBackgroundColor(resources.getColor(R.color.buttonColor, theme))
            setTextColor(resources.getColor(R.color.buttonTextColor, theme))
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 8, 0)
            }
        }
    }

    private fun showInspection(index: Int) {
        currentInspectionIndex = index // Track the current index
        inspectionData.forEachIndexed { i, view ->
            view.visibility = if (i == index) View.VISIBLE else View.GONE
        }
    }

    private fun startToDoActivity(hiveId: Int) {
        val intent = Intent(this, ToDoActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }

    private fun startAddToDoActivity(hiveId: Int, hiveIndex: Int) {
        lastToDoHiveIndex = hiveIndex // <-- track the view to update
        val intent = Intent(this, AddToDoActivity::class.java)
        intent.putExtra("hiveId", hiveId)
        toDoLauncher.launch(intent)
    }

}
