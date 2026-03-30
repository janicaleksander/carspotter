package com.example.carspotter.models

import androidx.room.Embedded
import androidx.room.Relation

data class CarWithDetails(
    @Embedded val car: Car,
    @Relation(
        parentColumn = "id",
        entityColumn = "carId"
    )
    val details: CarDetails?
)