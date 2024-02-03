package com.example.brokskeeping

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.brokskeeping.DataClasses.HumTempData
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.HumTempDataFunctionality
import com.example.brokskeeping.DbFunctionality.Utils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class AddLogActivity : AppCompatActivity() {
    private lateinit var buttonChooseFile: Button
    private lateinit var etFileName: TextView
    private lateinit var buttonAddData: Button
    private lateinit var db: DatabaseHelper
    private var stationId: Int = -1
    private var hiveId: Int = -1
    private var fileData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_log)

        buttonChooseFile = findViewById(R.id.btn_choose_file)
        etFileName = findViewById(R.id.edit_text_File_Name)
        buttonAddData = findViewById(R.id.btn_add_data)
        db = DatabaseHelper(this)

        stationId = intent.getIntExtra("stationId", -1)
        hiveId = intent.getIntExtra("hiveId", -1)

        buttonChooseFile.setOnClickListener {
            // Call a function to start the file explorer
            startFileExplorer()
        }

        buttonAddData.setOnClickListener {
            // Call a function to save hive information
            saveLog()
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
                    etFileName.text = "$fileName"
                    // Call the function to read file content and store it
                    fileData = readFileContent(uri)

                } else {
                    // Invalid file, show a Toast to the user
                    Toast.makeText(this, "Please choose a valid text file (.txt)", Toast.LENGTH_SHORT).show()
                    // Clear the file name from the UI
                    etFileName.text = "Selected File: None"
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

    private fun saveLog() {
        val selectedFile = etFileName.text.toString()

        // Check if the notes and file are valid
        if (Utils.isValidFile(selectedFile, fileData)) {
            // Call the database function to create a new hive
            val dateRange = Utils.getFirstAndLastDate(fileData)
            val data = HumTempData(stationId = stationId, hiveId = hiveId, logText = fileData, firstDate = dateRange.firstDate, lastDate = dateRange.lastDate)
            HumTempDataFunctionality.addDataLogs(db, data)
            Toast.makeText(this, "New hive added", Toast.LENGTH_SHORT).show()
            finish()

        } else {
            // Show a Toast indicating an issue with notes or file
            Toast.makeText(this, "Invalid file", Toast.LENGTH_SHORT).show()
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

    companion object {
        const val FILE_REQUEST_CODE = 1
    }
}
