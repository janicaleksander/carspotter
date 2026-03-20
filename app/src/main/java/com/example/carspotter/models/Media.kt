package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = Car::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Media(
@PrimaryKey(autoGenerate = true)
    val id:Long = 0,
    val carId:Long, // FK
    val userId:Long, //FK
    val type: MediaTypeEnum, // enum
    val filePath: String,
    val createdAt: LocalDateTime
)



        /*
        *
        * @Database(entities = [Media::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase()
        * */