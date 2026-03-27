package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

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
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val carId: String, // FK
    val userId: String?, //FK null if car from top cars
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