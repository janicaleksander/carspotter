package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "car",
    foreignKeys = [
        ForeignKey(
            entity = Brand::class,
            parentColumns = ["id"],
            childColumns = ["brand"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class Car(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val brand:String,// FK
    val model:String,
    val year: Int,
    val price: Double,
    val description:String,
    val category:String, //FK
    val isTop: Boolean,

    //These three only for top cars
    val powerHP:Int?,
    val acceleration: Double?,
    val maxSpeed:Double?,

    )
