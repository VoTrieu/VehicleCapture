package com.ttv.vehiclecapture

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ttv.vehiclecapture.data.VehicleRepository
import com.ttv.vehiclecapture.databinding.ActivityAddVehicleBinding
import com.ttv.vehiclecapture.model.Vehicle
import java.io.ByteArrayOutputStream

class AddVehicleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVehicleBinding

    private var vehiclePhotoBitmap: Bitmap? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photo = result.data?.extras?.getParcelable("data", Bitmap::class.java)

                if (photo != null) {
                    vehiclePhotoBitmap = photo
                    binding.vehiclePhotoImageView.setImageBitmap(photo)
                }
            }
        }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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




        binding.takePhotoButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
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

        val photoBase64 = vehiclePhotoBitmap?.let { bitmapToBase64(it) }

        val vehicle = Vehicle(
            id = VehicleRepository.getNextId(),
            makeModel = makeModel,
            year = year,
            mileage = mileage,
            notes = notes,
            photoUri = photoBase64
        )

        VehicleRepository.addVehicle(vehicle)

        Toast.makeText(this, getString(R.string.vehicle_saved_successfully), Toast.LENGTH_LONG).show()
        finish()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String{
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }


}