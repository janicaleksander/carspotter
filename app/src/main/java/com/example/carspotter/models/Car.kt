package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "car",
    foreignKeys = [
        ForeignKey(
            entity = Brand::class,
            parentColumns = ["name"],
            childColumns = ["brand"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["name"],
            childColumns = ["category"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class Car(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
