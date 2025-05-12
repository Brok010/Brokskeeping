package com.example.brokskeeping

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.NoteActivities.NotesBrowserActivity
import com.example.brokskeeping.databinding.ActivityBarcodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

class BarcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private var intentData: String = ""
    private lateinit var db: DatabaseHelper
    private var hiveId = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hiveId = intent.getIntExtra("hiveId", -1) // if a user is already on a hive we get the id of said hive
    }

    private fun iniBc() {
        barcodeDetector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)    //change this at some point
            .setAutoFocusEnabled(true)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .build()
        binding.surfaceView!!.holder.addCallback(object :SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")  //is this a good idea?
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(binding.surfaceView!!.holder)
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                // ?
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })
        barcodeDetector.setProcessor(object :Detector.Processor<Barcode>{
            override fun release() {
                cameraSource.stop()
            }
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if(barcodes.size()!=0) {
                    // Get the first detected barcode's value
                    intentData = barcodes.valueAt(0).rawValue
                    runOnUiThread { // Ensure UI updates are done on the main thread
                        processScannedData(intentData)
                    }
                }
            }
        })
    }

    private fun processScannedData(qrString: String) {
        cameraSource.stop() // Stop the camera once we have detected a barcode to prevent re-scanning
        startNotesBrowserActivity(qrString) // Start the next activity with the scanned QR data
    }
    override fun onPause() {
        super.onPause()
        cameraSource!!.release()
    }

    override fun onResume() {
        super.onResume()
        iniBc()
    }

    private fun startNotesBrowserActivity(qrString: String) {
        val intent = Intent(this, NotesBrowserActivity::class.java)
        intent.putExtra("qrString", qrString)
        intent.putExtra("hiveId", hiveId)
        startActivity(intent)
    }
}