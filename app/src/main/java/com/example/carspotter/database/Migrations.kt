package com.example.carspotter.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase



val SEED_CATEGORIES = listOf("HYPER", "ELECTRIC", "TRACK", "OLD","OTHER")
val SEED_BRANDS = listOf(
    "LAMBORGHINI", "FERRARI", "PORSCHE", "BUGATTI", "MCLAREN",
    "MERCEDES-BENZ", "AUDI", "BMW", "TESLA", "FORD",
    "CHEVROLET", "NISSAN", "TOYOTA", "HONDA", "VOLKSWAGEN",
    "HYUNDAI", "KIA", "SUBARU", "MAZDA", "JAGUAR",
    "LEXUS", "INFINITI", "ACURA", "ALFA ROMEO", "FIAT",
    "MITSUBISHI", "PEUGEOT", "RENAULT", "SEAT", "SKODA",
    "VOLVO", "CITROEN", "LAND ROVER", "ROLLS-ROYCE", "BENTLEY",
    "ASTON MARTIN","OTHER"
)

fun seedDatabase(db: SupportSQLiteDatabase) {
    SEED_CATEGORIES.forEach {
        db.execSQL("INSERT INTO category (name) VALUES (?)", arrayOf(it))
    }
    SEED_BRANDS.forEach {
        db.execSQL("INSERT INTO brand (name) VALUES (?)", arrayOf(it))
    }
}

