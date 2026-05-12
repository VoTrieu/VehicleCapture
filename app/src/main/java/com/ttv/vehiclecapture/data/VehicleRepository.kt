package com.ttv.vehiclecapture.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ttv.vehiclecapture.model.Vehicle
import androidx.core.content.edit

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

    private fun saveVehicles(context: Context){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val vehiclesJson = gson.toJson(vehicles)

        prefs.edit { putString(VEHICLES_KEY, vehiclesJson) }
    }
}