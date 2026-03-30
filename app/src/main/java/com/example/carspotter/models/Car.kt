package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "car",
    foreignKeys = [
        ForeignKey(Brand::class, ["id"], ["brandId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(Category::class, ["id"], ["categoryId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("brandId"), Index("categoryId")]
)
data class Car(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val brandId: String,
    val categoryId: String,
    val model: String,
    val year: Int,
    val price: Double,
    val isTop: Boolean,
)