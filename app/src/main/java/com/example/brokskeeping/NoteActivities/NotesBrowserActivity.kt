package com.example.brokskeeping.NoteActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var header: TextView
    private lateinit var btnLayout: LinearLayout

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
                Toast.makeText(this, "Hive not found", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "U are on the correct hive", Toast.LENGTH_SHORT).show()
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
        header.text = "Notes of [$stationName], [$hiveName]"

        // buttons
        btnLayout = binding.llCommonBrowserButtonLayout
        val addNoteButton = Button(this).apply {
            id = View.generateViewId()
            text = "Add Note"
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
    }

    override fun onResume() {
        super.onResume()
        val (updatedNotesList, result) = NotesFunctionality.getAllNotes(db, hiveId, null, null, true)
        if (result == 0) {
            Toast.makeText(this, "Couldn't retrieve notes", Toast.LENGTH_SHORT).show()
        }
        notesAdapter.updateData(updatedNotesList)
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