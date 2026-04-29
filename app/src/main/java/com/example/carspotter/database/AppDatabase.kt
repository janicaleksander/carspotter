package com.example.carspotter.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
import com.example.carspotter.dao.UserDreamDao
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
import com.example.carspotter.models.UserDream

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
        UserDream::class,
        Settings::class
    ],
    version = 3 //TODO change these every time we change migrations
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
    abstract fun userDreamDao(): UserDreamDao

    companion object {
        const val DATABASE_NAME = "carspotter_database"

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_media_carId ON media(carId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_media_carId_type ON media(carId, type)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_favourite_carId ON favourite(carId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_favourite_userId_carId ON favourite(userId, carId)"
                )
            }
        }

        fun build(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(MIGRATION_2_3)
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




