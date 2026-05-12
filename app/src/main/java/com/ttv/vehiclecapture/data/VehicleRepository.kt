package com.ttv.vehiclecapture.data

import com.ttv.vehiclecapture.model.Vehicle

object VehicleRepository {
    private val vehicles = mutableListOf<Vehicle>()

    fun getVehicles(): List<Vehicle>{
        return vehicles
    }

    fun addVehicle(vehicle: Vehicle){
        vehicles.add(vehicle)
    }

    fun getNextId(): Long{
        return System.currentTimeMillis()
    }
}