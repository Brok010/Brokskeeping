// BottomMenuFragment.kt
package com.example.brokskeeping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class BottomMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bottom_menu, container, false)

        // Find buttons by ID
        val backButton: Button = view.findViewById(R.id.backButton)
        val qrScanButton: Button = view.findViewById(R.id.qrScanButton)
        val settingsButton: Button = view.findViewById(R.id.settingsButton)

        // Set click listeners for buttons
        backButton.setOnClickListener {
            // Handle back button click
        }

        qrScanButton.setOnClickListener {
            // Handle QR scan button click
        }

        settingsButton.setOnClickListener {
            // Handle settings button click
        }

        return view
    }
}
