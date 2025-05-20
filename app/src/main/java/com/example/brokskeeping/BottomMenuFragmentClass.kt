// BottomMenuFragment.kt
package com.example.brokskeeping

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.OtherFunctionality
import com.example.brokskeeping.Functionality.Reused_functions
import com.example.brokskeeping.Functionality.Utils

class BottomMenuFragment : Fragment() {
    private lateinit var db: DatabaseHelper
    private lateinit var requestCamera: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DatabaseHelper(requireContext())

        // Initialize the permission launcher to handle camera permission requests
        requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, start the BarcodeScan activity TODO send in a hive id
                val hiveId = arguments?.getInt("hiveId", -1) ?: -1 // TODO check if this work i guess, then delete the comments
                val intent = Intent(requireActivity(), BarcodeScan::class.java)
                intent.putExtra("hiveId", hiveId)
                startActivity(intent)
            } else {
                // Permission not granted, show a toast message
                Toast.makeText(requireContext(),
                    getString(R.string.camera_permission_not_given), Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bottom_menu, container, false)

        // Find buttons by ID
        val qrScanButton: Button = view.findViewById(R.id.qrScanButton)
        val settingsButton: ImageButton = view.findViewById(R.id.settingsButton)

        // Set click listeners for buttons
        qrScanButton.setOnClickListener {
            requestCamera.launch(android.Manifest.permission.CAMERA)
        }

        settingsButton.setOnClickListener {
            Reused_functions.showSettingsMenu(requireContext(), it, db)
        }
        return view
    }
}
