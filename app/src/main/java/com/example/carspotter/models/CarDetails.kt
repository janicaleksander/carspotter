package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity(
    tableName = "car_detail",
    foreignKeys = [
        ForeignKey(Car::class, ["id"], ["carId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("carId", unique = true)]
)
data class CarDetails(
    @PrimaryKey
    val carId: String,
    val description: String,
    val powerHP: Int,
    val acceleration: Double,
    val maxSpeed: Double,
    val updatedAt: LocalDateTime

)