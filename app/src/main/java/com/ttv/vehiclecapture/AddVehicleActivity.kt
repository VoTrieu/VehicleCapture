package com.ttv.vehiclecapture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ttv.vehiclecapture.data.VehicleRepository
import com.ttv.vehiclecapture.databinding.ActivityAddVehicleBinding
import com.ttv.vehiclecapture.model.Vehicle
import java.io.File
import androidx.core.net.toUri

class AddVehicleActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        private const val TAG = "AddVehicleActivity"
    }


    private lateinit var binding: ActivityAddVehicleBinding

    private var editingVehicleId: Long? = null
    private var existingPhotoUri: String? = null
    private var currentPhotoUri: Uri? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && currentPhotoUri != null) {
                binding.vehiclePhotoImageView.setImageURI(currentPhotoUri)
                Log.d(TAG, "Camera thumbnail received")
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted){
                openCamera()
            }else{
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.addVehicleToolbar) { toolbar, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            toolbar.updatePadding(top = systemBars.top)

            insets
        }


        setSupportActionBar(binding.addVehicleToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_vehicle)

        editingVehicleId = intent.takeIf { it.hasExtra(EXTRA_VEHICLE_ID) }
            ?.getLongExtra(EXTRA_VEHICLE_ID, -1L)
            ?.takeIf { it != -1L }

        editingVehicleId?.let { setupEditMode(it) }

        binding.takePhotoButton.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        binding.saveVehicleButton.setOnClickListener {
            addNewOrUpdateVehicle()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun validateVehicleForm(
        makeModel: String,
        year: String,
        mileage: String
    ): Boolean {
        var isValid = true

        binding.makeModelInputLayout.error = null
        binding.yearInputLayout.error = null
        binding.mileageInputLayout.error = null

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val maxVehicleYear = currentYear + 1

        if (makeModel.isEmpty()) {
            binding.makeModelInputLayout.error = getString(R.string.enter_make_and_model)
            isValid = false
        }

        val yearNumber = year.toIntOrNull()

        if (year.isEmpty()) {
            binding.yearInputLayout.error = getString(R.string.enter_year)
            isValid = false
        }else if(yearNumber == null){
            binding.yearInputLayout.error = "Enter a valid year"
            isValid = false
        }else if (yearNumber !in 1900..maxVehicleYear){
            binding.yearInputLayout.error = "Year must be between 1900 and $maxVehicleYear"
            isValid = false
        }

        val mileageNumber = mileage.toIntOrNull()

        if (mileage.isEmpty()) {
            binding.mileageInputLayout.error = getString(R.string.enter_mileage)
            isValid = false
        } else if (mileageNumber == null){
            binding.mileageInputLayout.error = "Enter a valid mileage"
        } else if (mileageNumber < 0){
            binding.mileageInputLayout.error = "Mileage cannot be negative"
            isValid = false
        }

        return isValid
    }

    private fun addNewOrUpdateVehicle(){
        val makeModel = binding.makeModelEditText.text.toString().trim()
        val year = binding.yearEditText.text.toString().trim()
        val mileage = binding.mileageEditText.text.toString().trim()
        val notes = binding.notesEditText.text.toString().trim()

        val isValid = validateVehicleForm(
            makeModel = makeModel,
            year = year,
            mileage = mileage
        )

        if (!isValid) {
            return
        }

        val photoUri = currentPhotoUri?.toString() ?: existingPhotoUri

        val vehicle = Vehicle(
            id = editingVehicleId ?: VehicleRepository.getNextId(),
            makeModel = makeModel,
            year = year,
            mileage = mileage,
            notes = notes,
            photoUri = photoUri
        )

        if(editingVehicleId == null){
            Log.d(TAG, "Saving new vehicle: $makeModel")
            VehicleRepository.addVehicle(this, vehicle)
            Toast.makeText(this, getString(R.string.vehicle_saved_successfully), Toast.LENGTH_LONG).show()
        }else{
            Log.d(TAG, "Updating vehicle id: ${vehicle.id}")
            VehicleRepository.updateVehicle(this, vehicle)
            Toast.makeText(this, getString(R.string.vehicle_updated), Toast.LENGTH_LONG).show()
        }

        finish()
    }


    private fun setupEditMode(vehicleId: Long){
        val vehicle = VehicleRepository.getVehicleById(vehicleId) ?: return
        supportActionBar?.title = getString(R.string.edit_vehicle)
        binding.saveVehicleButton.text = getString(R.string.update_vehicle)

        binding.makeModelEditText.setText(vehicle.makeModel)
        binding.yearEditText.setText(vehicle.year)
        binding.mileageEditText.setText(vehicle.mileage)
        binding.notesEditText.setText(vehicle.notes)
        existingPhotoUri = vehicle.photoUri

        existingPhotoUri?.let {
            binding.vehiclePhotoImageView.setImageURI(existingPhotoUri!!.toUri())
        }


    }

    private fun checkCameraPermissionAndOpenCamera(){
        val permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if(permissionStatus ==PackageManager.PERMISSION_GRANTED){
            openCamera()
        }else{
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun openCamera(){
        currentPhotoUri = createPhotoUri()

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
        cameraLauncher.launch(cameraIntent)
    }

    private fun getCameraThumbnail(data: Intent?): Bitmap?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            data?.extras?.getParcelable("data", Bitmap::class.java)
        }else{
            @Suppress("DEPRECATION")
            data?.extras?.getParcelable("data")
        }
    }

    private fun createPhotoUri(): Uri{
        val photoDirectory = File(filesDir, "vehicle_photos")

        if(!photoDirectory.exists()){
            photoDirectory.mkdirs()
        }

        val photoFile = File(
            photoDirectory,
            "vehicle_${System.currentTimeMillis()}.jpg"
        )

        return FileProvider.getUriForFile(this, "${packageName}.file_provider", photoFile)
    }

}