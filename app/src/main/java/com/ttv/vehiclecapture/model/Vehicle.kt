package com.ttv.vehiclecapture.model

data class Vehicle (
    val id: Long,
    val makeModel: String,
    val year: String,
    val mileage: String,
    val notes: String,
    val photoUri: String? = null
)