package com.example.carspotter.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_0_1 = object : Migration(0, 1) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // blank - only to establish version 1
    }
}
val MIGRATION_1_2 = object : Migration(1,2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf("HYPER", "ELECTRIC", "TRACK", "OLD", "OTHER").forEach {
            db.execSQL("INSERT INTO category (name) VALUES (?)", arrayOf(it))
        }

        listOf(
            "LAMBORGHINI", "FERRARI", "PORSCHE", "BUGATTI", "MCLAREN",
            "MERCEDES-BENZ", "AUDI", "BMW", "TESLA", "FORD",
            "CHEVROLET", "NISSAN", "TOYOTA", "HONDA", "VOLKSWAGEN",
            "HYUNDAI", "KIA", "SUBARU", "MAZDA", "JAGUAR",
            "LEXUS", "INFINITI", "ACURA", "ALFA ROMEO", "FIAT",
            "MITSUBISHI", "PEUGEOT", "RENAULT", "SEAT", "SKODA",
            "VOLVO", "CITROEN", "LAND ROVER", "ROLLS-ROYCE", "BENTLEY",
            "ASTON MARTIN", "MCLAREN", "OTHER"
        ).forEach {
            db.execSQL("INSERT INTO brand (name) VALUES (?)", arrayOf(it))
        }
    }
}
