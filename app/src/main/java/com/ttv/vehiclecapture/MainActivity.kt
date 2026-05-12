package com.ttv.vehiclecapture

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.ttv.vehiclecapture.data.VehicleRepository
import com.ttv.vehiclecapture.databinding.ActivityMainBinding
import com.ttv.vehiclecapture.model.Vehicle
import com.ttv.vehiclecapture.ui.VehicleAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var vehicleAdapter: VehicleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicleAdapter = VehicleAdapter(emptyList<Vehicle>())
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.vehiclesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.vehiclesRecyclerView.adapter = vehicleAdapter

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainToolbar) { toolbar, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbar.updatePadding(top = systemBars.top)
            insets
        }



        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.title = getString(R.string.app_name)

        binding.addVehicleFab.setOnClickListener {
            val intent = Intent(this, AddVehicleActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateVehicleList()
    }

    private fun updateVehicleList(){
        val vehicles = VehicleRepository.getVehicles()
        vehicleAdapter.submitList(vehicles)

        if(vehicles.isEmpty()){
            binding.emptyStateTextView.visibility = View.VISIBLE
            binding.vehiclesRecyclerView.visibility = View.GONE
        }else{
            binding.emptyStateTextView.visibility = View.GONE
            binding.vehiclesRecyclerView.visibility = View.VISIBLE
        }
    }
}