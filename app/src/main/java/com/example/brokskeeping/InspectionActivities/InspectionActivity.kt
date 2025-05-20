package com.example.brokskeeping.InspectionActivities

import SupplementedFeed
import android.content.Context
import android.content.Intent
import com.example.brokskeeping.R
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.example.brokskeeping.DbFunctionality.StationsFunctionality
import com.example.brokskeeping.DbFunctionality.SupplementedFeedFunctionality
import com.example.brokskeeping.DbFunctionality.ToDoFunctionality
import com.example.brokskeeping.InspectionDataActivities.InspectionDataActivity
import com.example.brokskeeping.ToDoActivities.AddToDoActivity
import com.example.brokskeeping.ToDoActivities.ToDoActivity
import com.example.brokskeeping.databinding.ActivityInspectionContainerBinding
import com.google.android.material.slider.Slider
import java.util.Calendar
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
        val (data, result) = HivesFunctionality.getAllHives(db, stationId, 0, ordered = true)
        if (result == 0 || data.isEmpty()) {
            Toast.makeText(this, getString(R.string.could_not_load_hives), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        beehiveData = data // assign to the class property

        beehiveData!!.forEachIndexed { index, hive ->
            addInspectionView(hive, index)
        }
        showInspection(0) // Show first hive view only

        toDoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val currentView = inspectionData[lastToDoHiveIndex]
                    val checkboxToDo = currentView.findViewById<CheckBox>(R.id.checkbox_added_to_do)

                    checkboxToDo?.isChecked = true
                }
            }
    }

    private fun addInspectionView(hive: Beehive, index: Int) {
        val inflater = LayoutInflater.from(this)
        val inspectionView =
            inflater.inflate(R.layout.view_inspection_content, inspectionContainer, false)

        // Fill in hive-specific data
        inspectionView.findViewById<TextView>(R.id.tv_hive_name).text = hive.nameTag

        val spinner = inspectionView.findViewById<Spinner>(R.id.spinner_frames_per_super)
        val framesPerSuperOptions = (1..18).map { it.toString() }
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, framesPerSuperOptions)
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

        if (hive.supers >= 1) {
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
                askAdjustment(getString(R.string.brood_frames)) { result ->
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
                askAdjustment(getString(R.string.honey_frames)) { result ->
                    honeyAdjustedCheckbox.tag = result
                }
            } else {
                honeyAdjustedCheckbox.tag = 0
            }
        }

        val droneAdjustedCheckbox = inspectionView.findViewById<CheckBox>(R.id.checkbox_drone_taken)
        droneAdjustedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                askAdjustment(getString(R.string.drone_brood_frames)) { result ->
                    droneAdjustedCheckbox.tag = result
                }
            } else {
                droneAdjustedCheckbox.tag = 0
            }
        }

        // honey harvest
        val honeyHarvested = inspectionView.findViewById<TextView>(R.id.honey_frames_harvested)
        honeyHarvested.text = ""

        // separation and join
        val separationCheckbox = inspectionView.findViewById<CheckBox>(R.id.checkbox_separate)
        separationCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                separationPopUp(hive, db, this, separationCheckbox)
            } else {
                separationCheckbox.tag = -1
            }
        }
        val joinCheckbox = inspectionView.findViewById<CheckBox>(R.id.checkbox_join)
        joinCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                joinPopUp(hive, db, this, joinCheckbox)
            } else {
                joinCheckbox.tag = 0 // Reset if unchecked
            }
        }

        // slider
        val aggressivitySlider = inspectionView.findViewById<Slider>(R.id.aggressivity_slider)
        val initialValueAggressivity =
            if (hive.aggressivity < 1 || hive.aggressivity > 5) 1 else hive.aggressivity
        aggressivitySlider.value = initialValueAggressivity.toFloat()

        val attentionSlider = inspectionView.findViewById<Slider>(R.id.slider_attention_worth)
        val initialValueAttention =
            if (hive.attentionWorth < 1 || hive.attentionWorth > 5) 1 else hive.attentionWorth
        attentionSlider.value = initialValueAttention.toFloat()

        // supplemented feed
        val supplementedFeedCheckBox = inspectionView.findViewById<CheckBox>(R.id.checkbox_supplemented_feed)
        supplementedFeedCheckBox.setOnCheckedChangeListener(supplementedFeedPopUp())

        // ToD0's
        val btnAddToDo = inspectionView.findViewById<Button>(R.id.bt_add_todo)
        val (getPendingToDos, _) = ToDoFunctionality.getAllToDos(db, hive.id, 0, 0, 1, true)
        btnAddToDo.text = getString(R.string.pending_todo_s, getPendingToDos.size)

        btnAddToDo.setOnClickListener {
            if (getPendingToDos.isNotEmpty()) {
                val options = arrayOf(getString(R.string.add_new_to_do),
                    getString(R.string.show_pending_to_dos))
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.choose_an_option_for_to_do))
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

        // notes
        val btnLastNotes = inspectionView.findViewById<Button>(R.id.btn_last_notes)
        btnLastNotes.setOnClickListener {
            showLastNotes(hive.id, this)
        }

        // last inspection
        val btnLastInspection = inspectionView.findViewById<Button>(R.id.btn_last_inspection)
        btnLastInspection.setOnClickListener {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val (inspectionData, result) = InspectionsFunctionality.getAllInspectionDataForHiveId(
                db,
                hive.id,
                currentYear,
                null,
                true
            )
            if (result == 0 || inspectionData.isEmpty()) {
                Toast.makeText(this,
                    getString(R.string.no_previous_inspections_this_year), Toast.LENGTH_SHORT).show()
            } else {
                startInspectionDataActivity(inspectionData[0].id)
            }
        }

        inspectionView.visibility = View.GONE // Hidden until selected
        inspectionData.add(inspectionView)
        inspectionContainer!!.addView(inspectionView)
    }

    private fun startInspectionDataActivity(inspectionDataId: Int) {
        val intent = Intent(this, InspectionDataActivity::class.java)
        intent.putExtra("inspectionDataId", inspectionDataId)
        startActivity(intent)
    }

    private fun showLastNotes(hiveId: Int, context: Context) {
        val (notes, result) = NotesFunctionality.getAllNotes(db, hiveId, orderByDate = true)

        if (result != 0 && notes.isNotEmpty()) {
            val lastNote = notes[0].noteText

            AlertDialog.Builder(context)
                .setTitle(getString(R.string.last_notes))
                .setMessage(lastNote)
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        } else {
            Toast.makeText(context, getString(R.string.couldn_t_retrieve_note), Toast.LENGTH_SHORT).show()
        }
    }

    private fun joinPopUp(hive: Beehive, db: DatabaseHelper, context: Context, checkbox: CheckBox) {
        val (stations, stationResult) = StationsFunctionality.getAllStations(db, 1)

        if (stationResult == 0 || stations.isEmpty()) {
            Toast.makeText(context, getString(R.string.cannot_find_stations), Toast.LENGTH_SHORT).show()
            checkbox.isChecked = false
            return
        }

        val stationNames = stations.map { it.name }.toTypedArray()

        val stationDialog = AlertDialog.Builder(context)
        stationDialog.setTitle(getString(R.string.select_station_to_join_into))
        stationDialog.setItems(stationNames) { _, stationIndex ->
            val selectedStationId = stations[stationIndex].id

            val (allHives, hiveResult) = HivesFunctionality.getAllHives(db, selectedStationId, 0, ordered = true)
            val hives = allHives.filter { it.id != hive.id }

            if (hiveResult == 0 || hives.isEmpty()) {
                Toast.makeText(context,
                    getString(R.string.cannot_find_hives_in_selected_station), Toast.LENGTH_SHORT)
                    .show()
                checkbox.isChecked = false
                return@setItems
            }

            val hiveNames = hives.map { it.nameTag }.toTypedArray()

            AlertDialog.Builder(context)
                .setTitle(getString(R.string.select_hive_to_join_into))
                .setItems(hiveNames) { _, hiveIndex ->
                    val selectedHiveId = hives[hiveIndex].id
                    val newHive = hive.copy()
                    newHive.colonyEndState = selectedHiveId
                    HivesFunctionality.saveHive(db, newHive)
                    checkbox.tag = selectedHiveId
                }
                .setOnCancelListener {
                    checkbox.isChecked = false
                }
                .show()
        }

        stationDialog.setOnCancelListener {
            checkbox.isChecked = false
        }

        stationDialog.show()
    }

    private fun supplementedFeedPopUp(): (CompoundButton, Boolean) -> Unit {
        return { buttonView, isChecked ->
            if (isChecked) {
                val input = EditText(buttonView.context).apply {
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    hint = context.getString(R.string.enter_kilos)
                }

                AlertDialog.Builder(buttonView.context)
                    .setTitle(getString(R.string.supplemented_feed))
                    .setMessage(getString(R.string.how_many_kilos))
                    .setView(input)
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        val value = input.text.toString().toDoubleOrNull()
                        if (value != null && value > 0) {
                            buttonView.tag = value
                        } else {
                            Toast.makeText(
                                buttonView.context,
                                getString(R.string.invalid_input_please_try_again),
                                Toast.LENGTH_SHORT
                            ).show()
                            buttonView.isChecked = false
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        buttonView.isChecked = false
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                buttonView.tag = null
            }
        }
    }

    private fun separationPopUp(hive: Beehive, db: DatabaseHelper, context: Context, checkbox: CheckBox) {
        val (stations, result) = StationsFunctionality.getAllStations(db, 1)

        if (result == 0 || stations.isEmpty()) {
            Toast.makeText(context, getString(R.string.cannot_find_stations), Toast.LENGTH_SHORT).show()
            checkbox.isChecked = false
            return
        }

        val stationNames = stations.map { it.name }.toTypedArray()

        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.select_station_for_new_hive))
        builder.setItems(stationNames) { _, which ->
            val selectedStationId = stations[which].id

            // Prompt for hive name
            val nameInput = EditText(context)
            nameInput.hint = getString(R.string.enter_new_hive_name)

            AlertDialog.Builder(context)
                .setTitle(getString(R.string.new_hive_name))
                .setView(nameInput)
                .setPositiveButton(getString(R.string.create)) { _, _ ->
                    val newName = nameInput.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        val newHive = Beehive().apply {
                            stationId = selectedStationId
                            nameTag = newName
                            colonyEndState = -1
                            colonyOrigin = hive.id.toString()
                        }
                        val (newHiveId, saveHiveResult) = HivesFunctionality.saveHive(db, newHive, "", "")
                        checkbox.tag = newHiveId
                        if (saveHiveResult == 0) {
                            Toast.makeText(context,
                                getString(R.string.could_not_save_new_hive), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context,
                            getString(R.string.hive_name_cannot_be_empty), Toast.LENGTH_SHORT).show()
                        checkbox.isChecked = false
                    }
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    checkbox.isChecked = false
                }
                .show()
        }
        builder.setOnCancelListener {
            checkbox.isChecked = false
        }
        builder.show()
    }


    private fun skip() {
        if (inspectionData.isNotEmpty() && currentInspectionIndex >= 0) {
            // Remove the current view from the list
            inspectionData.removeAt(currentInspectionIndex)
            inspectionContainer?.removeViewAt(currentInspectionIndex)

            beehiveData = beehiveData!!.toMutableList().apply {
                removeAt(currentInspectionIndex)
            }

            // Adjust the current inspection index
            if (currentInspectionIndex >= inspectionData.size) {
                currentInspectionIndex = inspectionData.size - 1 // Move to the last valid view
            }

            // If currentInspectionIndex is now -1 (empty list), hide the inspection container
            if (inspectionData.isEmpty()) {
                inspectionContainer?.visibility = View.GONE
            } else {
                // Show the next view, or no view if the list is empty
                showInspection(currentInspectionIndex)
            }
            regenerateNavigationButtons()
        }
    }

    private fun askAdjustment(frameType: String, onResult: (Int) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("$frameType Adjusted")
        builder.setMessage(getString(R.string.did_you_add_or_remove, frameType))

        builder.setPositiveButton(getString(R.string.added)) { _, _ ->
            askHowMany { amount -> onResult(amount) } // Positive
        }

        builder.setNegativeButton(getString(R.string.removed)) { _, _ ->
            askHowMany { amount -> onResult(-amount) } // Negative
        }

        builder.setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun askHowMany(onAmountEntered: (Int) -> Unit) {
        val input = EditText(this).apply {
            hint = context.getString(R.string.enter_number_of_frames)
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            addView(TextView(this@InspectionActivity).apply {
                text = context.getString(R.string.how_many)
                setPadding(0, 0, 0, 10)
            })
            addView(input)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.specify_amount))
            .setView(layout)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val amount = input.text.toString().toIntOrNull() ?: 0
                onAmountEntered(amount)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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


            val winterReadyCheckbox = view.findViewById<CheckBox>(R.id.checkbox_winter_ready)

            val broodFrames = broodFramesEditText.text.toString().toIntOrNull() ?: 0
            val honeyFrames = honeyFramesEditText.text.toString().toIntOrNull() ?: 0
            val droneFrames = droneFramesEditText.text.toString().toIntOrNull() ?: 0

            // For change detection (adjustments), assume changes were saved to a tag on the checkbox
            val broodChange = view.findViewById<CheckBox>(R.id.checkbox_brood_taken).tag as? Int ?: 0
            val honeyChange = view.findViewById<CheckBox>(R.id.checkbox_honey_taken).tag as? Int ?: 0
            val droneChange = view.findViewById<CheckBox>(R.id.checkbox_drone_taken).tag as? Int ?: 0
            val supplementedFeedTag = view.findViewById<CheckBox>(R.id.checkbox_supplemented_feed).tag as? Double ?: 0.0
            val honeyHarvested = view.findViewById<EditText>(R.id.honey_frames_harvested).text.toString().toIntOrNull() ?: 0

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
                Toast.makeText(this,
                    getString(R.string.addnote_function_crashed), Toast.LENGTH_SHORT).show()
                finish()
            }

            val aggressivity = view.findViewById<Slider>(R.id.aggressivity_slider).value.toInt()
            val attention = view.findViewById<Slider>(R.id.slider_attention_worth).value.toInt()

            val separateCheckbox = view.findViewById<CheckBox>(R.id.checkbox_separate)
            val separatedHiveId = (separateCheckbox.tag as? Int) ?: -1

            val joinedCheckbox = view.findViewById<CheckBox>(R.id.checkbox_join)
            val joinedHiveId = (joinedCheckbox.tag as? Int) ?: -1

            val deathCheckbox = view.findViewById<CheckBox>(R.id.checkbox_dead)
            var colonyEndState = -1

            if (deathCheckbox.isChecked) {
                colonyEndState = 0
            }

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
                supplementedFeed = supplementedFeedTag,
                framesPerSuper = framesPerSuper,
                freeSpaceFrames = freeSpaceFrames,
                winterReady = winterReadyCheckbox.isChecked,
                aggressivity = aggressivity,
                honeyHarvested = honeyHarvested,
                attentionWorth = attention,
                colonyEndState = colonyEndState,
                separated = separatedHiveId,
                joined = joinedHiveId
            )
            var deathTime = Date(0)
            if (joinedHiveId != -1) {
                deathTime = Date(System.currentTimeMillis())
                colonyEndState = joinedHiveId
            } else if (colonyEndState != -1) {
                deathTime = Date(System.currentTimeMillis())
            }

            val beehive = hive.copy(
                broodFrames = broodFrames,
                honeyFrames = honeyFrames,
                framesPerSuper = framesPerSuper,
                supers = superCount,
                droneBroodFrames = droneFrames,
                freeSpaceFrames = freeSpaceFrames,
                winterReady = winterReadyCheckbox.isChecked,
                aggressivity = aggressivity,
                attentionWorth = attention,
                colonyEndState = colonyEndState,
                deathTime = deathTime
            )

            val (insertedId, _) = InspectionsFunctionality.saveInspectionData(db, inspectionData)
            insertedInspectionIds.add(insertedId)
            HivesFunctionality.saveHive(db, beehive)

            val honeyHarvest = HoneyHarvest(
                id = -1,
                hiveId = hive.id,
                date = Date(),
                honeyFrames = honeyHarvested,
            )
            HoneyHarvestFunctionality.saveHoneyHarvest(db, honeyHarvest)

            val supplementedFeed = SupplementedFeed(
                id = -1,
                hiveId = hive.id,
                date = Date(),
                kilos = supplementedFeedTag
            )
            SupplementedFeedFunctionality.saveSupplementedFeed(db, supplementedFeed)
        }

        val inspection = Inspection(
            stationId = stationId,
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
        regenerateNavigationButtons()
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

    private fun regenerateNavigationButtons() {
        val currentView = inspectionData[currentInspectionIndex]

        // Find the button layout
        val btnLayout = currentView.findViewById<LinearLayout>(R.id.btn_layout)
        btnLayout.removeAllViews()

        // Add Previous button if we're not at the first inspection
        if (currentInspectionIndex > 0) {
            btnLayout.addView(createNavButton(getString(R.string.previous)) {
                showInspection(currentInspectionIndex - 1)
            })
        }

        // Add Next button if we're not at the last inspection
        if (currentInspectionIndex < inspectionData.size - 1) {
            btnLayout.addView(createNavButton(getString(R.string.next)) {
                showInspection(currentInspectionIndex + 1)
            })
            btnLayout.addView(createNavButton(getString(R.string.skip)) {
                skip()
            })
        } else {
            // If at the last inspection, show "Save All" and "Skip and Save All"
            btnLayout.addView(createNavButton(getString(R.string.save_all)) {
                saveAllToDatabase()
            })
            btnLayout.addView(createNavButton(getString(R.string.skip_and_save_all)) {
                skip()
                saveAllToDatabase()
            })
        }
    }
}
