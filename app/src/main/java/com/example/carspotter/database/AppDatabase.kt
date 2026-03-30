package com.example.carspotter.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.carspotter.dao.BrandDao
import com.example.carspotter.dao.CarDao
import com.example.carspotter.dao.CarDetailsDao
import com.example.carspotter.dao.CategoryDao
import com.example.carspotter.dao.FavouriteDao
import com.example.carspotter.dao.MediaDao
import com.example.carspotter.dao.SettingDao
import com.example.carspotter.dao.UserCarDao
import com.example.carspotter.dao.UserDao
import com.example.carspotter.models.Brand
import com.example.carspotter.models.Car
import com.example.carspotter.models.CarDetails
import com.example.carspotter.models.Category
import com.example.carspotter.models.Converters
import com.example.carspotter.models.Favourite
import com.example.carspotter.models.Media
import com.example.carspotter.models.Settings
import com.example.carspotter.models.User
import com.example.carspotter.models.UserCar

@Database(
    entities = [
        User::class,
        Brand::class,
        Category::class,
        Car::class,
        CarDetails::class,
        UserCar::class,
        Media::class,
        Favourite::class,
        Settings::class
    ],
    version = 1 //TODO change these every time we change migrations
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun brandDao(): BrandDao
    abstract fun categoryDao(): CategoryDao
    abstract fun carDao(): CarDao
    abstract fun userCarDao(): UserCarDao
    abstract fun mediaDao(): MediaDao
    abstract fun settingDao(): SettingDao
    abstract fun favouriteDao(): FavouriteDao

    abstract fun carDetailsDao(): CarDetailsDao


    companion object {
        const val DATABASE_NAME = "carspotter_database"

        fun build(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    //seedDatabase(db)

                }
            })
            .fallbackToDestructiveMigration(true)
            .build()
    }
}




