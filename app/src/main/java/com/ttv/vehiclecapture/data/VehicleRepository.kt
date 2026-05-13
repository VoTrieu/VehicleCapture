package com.ttv.vehiclecapture.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ttv.vehiclecapture.model.Vehicle
import androidx.core.content.edit
import androidx.core.net.toUri
import java.io.File

object VehicleRepository {
    private val vehicles = mutableListOf<Vehicle>()
    private const val PREFS_NAME = "vehicle_capture_prefs"
    private const val VEHICLES_KEY = "vehicles"
    private val gson = Gson()

    fun loadVehicles(context: Context){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val vehiclesJson = prefs.getString(VEHICLES_KEY, null)
        if(vehiclesJson != null){
            val type = object : TypeToken<List<Vehicle>>() {}.type
            val savedVehicles: List<Vehicle> = gson.fromJson(vehiclesJson, type)

            vehicles.clear()
            vehicles.addAll(savedVehicles)
        }
    }

    fun getVehicles(): List<Vehicle>{
        return vehicles
    }

    fun addVehicle(context: Context, vehicle: Vehicle){
        vehicles.add(vehicle)
        saveVehicles(context)
    }

    fun getNextId(): Long{
        return System.currentTimeMillis()
    }

    fun getVehicleById(vehicleId: Long): Vehicle? {
        return vehicles.find { vehicle -> vehicle.id == vehicleId }
    }

    fun updateVehicle(context: Context, updatedVehicle: Vehicle){
        val index = vehicles.indexOfFirst { vehicle ->
            vehicle.id == updatedVehicle.id
        }

        if(index != -1){
            vehicles[index] = updatedVehicle
            saveVehicles(context)
        }
    }

    private fun saveVehicles(context: Context){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val vehiclesJson = gson.toJson(vehicles)

        prefs.edit { putString(VEHICLES_KEY, vehiclesJson) }
    }

    fun deleteVehicle(context: Context, vehicleId: Long){
        val vehicleToDelete = vehicles.find { vehicle ->
            vehicle.id == vehicleId
        }

        if(vehicleToDelete?.photoUri != null){
            deletePhotoFile(context, vehicleToDelete.photoUri)
        }

        vehicles.removeAll { vehicle ->
            vehicle.id == vehicleId
        }

        saveVehicles(context)
    }

    private fun deletePhotoFile(context: Context, photoUri: String){
        val uri = photoUri.toUri()
        if(uri.scheme == "content"){
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: return
            val photoFile = File(File(context.filesDir, "vehicle_photos"), fileName)

            if (photoFile.exists()){
                photoFile.delete()
            }
        }
    }
}