package com.example.brokskeeping.HiveActivities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.brokskeeping.DataClasses.Beehive
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HivesFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class AddHiveActivity : AppCompatActivity() {
    private lateinit var editTextNameTag: EditText
    private lateinit var editTextNotes: EditText
    private lateinit var editTextAddQRString: EditText
    private lateinit var buttonScanQR: Button
    private lateinit var buttonChooseFile: Button
    private lateinit var textViewFileName: TextView
    private lateinit var buttonSaveHive: Button
    private lateinit var db: DatabaseHelper
    private var stationId: Int = -1
    private var fileData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_hive)

        editTextNameTag = findViewById(R.id.editTextHiveNameTag)
        editTextNotes = findViewById(R.id.editTextHiveNotes)
        editTextAddQRString = findViewById(R.id.et_add_hive_qr_string)
        buttonScanQR = findViewById(R.id.btn_scan_QR)
        buttonChooseFile = findViewById(R.id.buttonChooseFile)
        textViewFileName = findViewById(R.id.textViewFileName)
        buttonSaveHive = findViewById(R.id.buttonSaveHive)
        db = DatabaseHelper(this)

        stationId = intent.getIntExtra("stationId", -1)


        buttonScanQR.setOnClickListener {
            //Todo
        }
        buttonChooseFile.setOnClickListener {
            startFileExplorer()
        }

        buttonSaveHive.setOnClickListener {
            saveHive()
        }
    }

    private fun startFileExplorer() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileName(uri)

                if (fileName?.endsWith(".txt") == true) {
                    // Valid text file, update the UI
                    textViewFileName.text = fileName
                    // Call the function to read file content and store it
                    fileData = readFileContent(uri)

                } else {
                    // Invalid file, show a Toast to the user
                    Toast.makeText(this,
                        getString(R.string.please_choose_a_valid_text_file_txt), Toast.LENGTH_SHORT).show()
                    // Clear the file name from the UI
                    textViewFileName.text = getString(R.string.none)
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val fileName = cursor?.getString(nameIndex ?: -1)
        cursor?.close()
        return fileName
    }


    private fun saveHive() {
        val name = editTextNameTag.text.toString()
        val qr = editTextAddQRString.text.toString()
        val broodFrames = findViewById<EditText>(R.id.tv_brood_frames).text.toString().toIntOrNull() ?: 0
        val honeyFrames = findViewById<EditText>(R.id.et_honey_frames).text.toString().toIntOrNull() ?: 0
        val droneBroodFrames = findViewById<EditText>(R.id.et_drone_brood_frames).text.toString().toIntOrNull() ?: 0
        val framesPerSuper = findViewById<EditText>(R.id.et_frames_per_super).text.toString().toIntOrNull() ?: 0
        val supers = findViewById<EditText>(R.id.et_supers).text.toString().toIntOrNull() ?: 0
        val supplementedFeed = findViewById<EditText>(R.id.et_supplemented_feed).text.toString().toIntOrNull() ?: 0
        val colonyOrigin = findViewById<EditText>(R.id.et_colony_origin).text.toString()

        val isWinterReady = findViewById<android.widget.CheckBox>(R.id.checkbox_winter_ready).isChecked
        val aggressivity = findViewById<com.google.android.material.slider.Slider>(R.id.slider_aggressivity_slider).value.toInt()
        val attentionWorth = findViewById<com.google.android.material.slider.Slider>(R.id.slider_attention_worth).value.toInt()

        val beehive = Beehive(
            id = -1,
            stationId = stationId,
            nameTag = name,
            qrTag = qr,
            framesPerSuper = framesPerSuper,
            supers = supers,
            broodFrames = broodFrames,
            honeyFrames = honeyFrames,
            droneBroodFrames = droneBroodFrames,
            freeSpaceFrames = framesPerSuper * supers - (broodFrames + honeyFrames + droneBroodFrames),
            colonyOrigin = colonyOrigin,
            winterReady = isWinterReady,
            aggressivity = aggressivity,
            colonyEndState = -1,
            attentionWorth = attentionWorth
        )
        val notes = editTextNotes.text.toString()
        val selectedFile = textViewFileName.text.toString().removePrefix(getString(R.string.selected_file))

        if (Utils.notesFormat(notes) && isValidFile(selectedFile, fileData)) {
            HivesFunctionality.saveHive(db, beehive, fileData, notes)
            Toast.makeText(this, getString(R.string.new_hive_added), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.invalid_notes_or_file), Toast.LENGTH_SHORT).show()
            return
        }
    }
    private fun readFileContent(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)

        return try {
            // Read the contents of the file into a string
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            "" // Return an empty string if there was an error reading the file
        } finally {
            inputStream?.close()
        }
    }

    private fun isValidFile(fileName: String, fileData: String): Boolean {
        return fileName.endsWith(".txt") || fileName == getString(R.string.none)
    }


    companion object {
        const val FILE_REQUEST_CODE = 1
    }
}