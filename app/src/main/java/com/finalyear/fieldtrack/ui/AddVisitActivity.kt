package com.finalyear.fieldtrack.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.finalyear.fieldtrack.databinding.ActivityAddVisitBinding
import com.finalyear.fieldtrack.db.DatabaseHelper
import com.finalyear.fieldtrack.model.Visit
import com.finalyear.fieldtrack.utils.LocationHelper
import com.finalyear.fieldtrack.utils.SmsHelper
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File
import java.util.Locale

/**
 * Screen where a field agent logs one visit.
 * Wires together: Camera, Local File Storage, GPS/Location, Speech-to-Text,
 * QR code scanning, SQLite insert, and an optional SMS notification.
 */
class AddVisitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVisitBinding
    private lateinit var db: DatabaseHelper
    private lateinit var locationHelper: LocationHelper

    private var photoUri: Uri? = null
    private var photoFile: File? = null
    private var capturedLat: Double = 0.0
    private var capturedLng: Double = 0.0

    // ---- Camera result launcher ----
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.photoPreview.setImageURI(photoUri)
        }
    }

    // ---- Speech to text launcher ----
    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!text.isNullOrEmpty()) {
                val existing = binding.notesInput.text.toString()
                binding.notesInput.setText(if (existing.isBlank()) text else "$existing $text")
            }
        }
    }

    // ---- QR scan launcher (ZXing embedded) ----
    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            binding.siteNameInput.setText(result.contents)
            Toast.makeText(this, "QR scanned: ${result.contents}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)
        locationHelper = LocationHelper(this)

        binding.btnCapturePhoto.setOnClickListener { launchCamera() }
        binding.btnGetLocation.setOnClickListener { fetchLocation() }
        binding.btnSpeechNotes.setOnClickListener { launchSpeechToText() }
        binding.btnScanQr.setOnClickListener { launchQrScanner() }
        binding.btnSaveVisit.setOnClickListener { saveVisit() }
    }

    // ---------- Camera + Local File Storage ----------
    private fun launchCamera() {
        val fileName = "visit_${System.currentTimeMillis()}.jpg"
        photoFile = File(getExternalFilesDir("photos"), fileName).also {
            it.parentFile?.mkdirs()
        }
        photoUri = FileProvider.getUriForFile(
            this, "$packageName.fileprovider", photoFile!!
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(intent)
    }

    // ---------- GPS ----------
    private fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission needed", Toast.LENGTH_SHORT).show()
            return
        }
        locationHelper.getCurrentLocation(
            onResult = { lat, lng ->
                capturedLat = lat
                capturedLng = lng
                binding.locationText.text = String.format(Locale.US, "Lat: %.5f, Lng: %.5f", lat, lng)
            },
            onError = {
                Toast.makeText(this, "Could not fetch location", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ---------- Speech to Text ----------
    private fun launchSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your visit notes")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- QR Code Scan ----------
    private fun launchQrScanner() {
        val options = ScanOptions().apply {
            setPrompt("Scan the asset / site QR code")
            setBeepEnabled(true)
            setOrientationLocked(true)
        }
        qrLauncher.launch(options)
    }

    // ---------- Save (SQLite insert) + optional SMS ----------
    private fun saveVisit() {
        val site = binding.siteNameInput.text.toString().trim()
        if (site.isEmpty()) {
            binding.siteNameInput.error = "Required"
            return
        }
        val visit = Visit(
            siteName = site,
            notes = binding.notesInput.text.toString().trim(),
            latitude = capturedLat,
            longitude = capturedLng,
            photoPath = photoFile?.absolutePath,
            timestamp = System.currentTimeMillis()
        )
        db.insertVisit(visit)

        val phone = binding.notifyPhoneInput.text.toString().trim()
        if (phone.isNotEmpty() &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            SmsHelper.sendVisitNotification(this, phone, site)
        }

        Toast.makeText(this, "Visit saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
