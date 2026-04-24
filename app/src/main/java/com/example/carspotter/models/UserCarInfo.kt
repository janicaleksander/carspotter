package com.example.carspotter.models

import androidx.room.Embedded
import androidx.room.Relation

data class UserCarInfo(
    @Embedded val car: Car,
    @Relation(
        parentColumn = "id",
        entityColumn = "carId"
    )
    val info: UserCar?,
)
