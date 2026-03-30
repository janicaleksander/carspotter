package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "category")
data class Category(
    @PrimaryKey
    val id:String = UUID.randomUUID().toString(),
    val name:String
)
