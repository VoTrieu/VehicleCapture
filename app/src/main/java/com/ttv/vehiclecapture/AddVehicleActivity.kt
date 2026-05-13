package com.ttv.vehiclecapture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ttv.vehiclecapture.data.VehicleRepository
import com.ttv.vehiclecapture.databinding.ActivityAddVehicleBinding
import com.ttv.vehiclecapture.model.Vehicle
import java.io.ByteArrayOutputStream

class AddVehicleActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        private const val TAG = "AddVehicleActivity"
    }


    private lateinit var binding: ActivityAddVehicleBinding

    private var vehiclePhotoBitmap: Bitmap? = null
    private var editingVehicleId: Long? = null
    private var existingPhotoBase64: String? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photo = getCameraThumbnail(result.data)

                if (photo != null) {
                    Log.d(TAG, "Camera thumbnail received")
                    vehiclePhotoBitmap = photo
                    binding.vehiclePhotoImageView.setImageBitmap(photo)
                }
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
            addNewVehicle()
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

        if (makeModel.isEmpty()) {
            binding.makeModelInputLayout.error = getString(R.string.enter_make_and_model)
            isValid = false
        }

        if (year.isEmpty()) {
            binding.yearInputLayout.error = getString(R.string.enter_year)
            isValid = false
        }

        if (mileage.isEmpty()) {
            binding.mileageInputLayout.error = getString(R.string.enter_mileage)
            isValid = false
        }

        return isValid
    }

    private fun addNewVehicle(){
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

        val photoBase64 = vehiclePhotoBitmap?.let { bitmapToBase64(it) } ?: existingPhotoBase64

        val vehicle = Vehicle(
            id = editingVehicleId ?: VehicleRepository.getNextId(),
            makeModel = makeModel,
            year = year,
            mileage = mileage,
            notes = notes,
            photoUri = photoBase64
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

    private fun bitmapToBase64(bitmap: Bitmap): String{
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun setupEditMode(vehicleId: Long){
        val vehicle = VehicleRepository.getVehicleById(vehicleId) ?: return
        supportActionBar?.title = getString(R.string.edit_vehicle)
        binding.saveVehicleButton.text = getString(R.string.update_vehicle)

        binding.makeModelEditText.setText(vehicle.makeModel)
        binding.yearEditText.setText(vehicle.year)
        binding.mileageEditText.setText(vehicle.mileage)
        binding.notesEditText.setText(vehicle.notes)
        existingPhotoBase64 = vehicle.photoUri

        existingPhotoBase64?.let {
            val bitmap = base64ToBitmap(it)
            binding.vehiclePhotoImageView.setImageBitmap(bitmap)
        }


    }

    private fun base64ToBitmap(base64: String): Bitmap?{
        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
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
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
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

}