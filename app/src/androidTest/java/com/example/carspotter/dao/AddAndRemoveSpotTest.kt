package com.example.carspotter.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carspotter.database.AppDatabase
import com.example.carspotter.models.Brand
import com.example.carspotter.models.Car
import com.example.carspotter.models.Category
import com.example.carspotter.models.Location
import com.example.carspotter.models.Media
import com.example.carspotter.models.MediaTypeEnum
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.User
import com.example.carspotter.models.UserCar
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.UserCarRepository
import io.appwrite.Client
import io.appwrite.services.Storage
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AddAndRemoveSpotTest {

    private companion object {
        val NOW           = LocalDateTime.of(2026, 5, 15, 12, 0)
         val USER_ID  = UUID.randomUUID().toString()
         val BRAND_ID = UUID.randomUUID().toString()
         val CAT_ID   = UUID.randomUUID().toString()

         val DEFAULT_MODEL      = "M3"
         val DEFAULT_YEAR       = 2024
         val DEFAULT_PRICE      = 90_000.0
         val DEFAULT_NOTES      = "Seen at Warsaw meet"
         val DEFAULT_MEDIA_PATH = "/storage/photo.jpg"

         val LAT          = 52.2297
         val LNG          = 21.0122
         val LAT_DELTA    = 0.0001

        val DEFAULT_LOCATION = Location(latitude = LAT, longitude = LNG)
    }

    private lateinit var db: AppDatabase
    private lateinit var carRepository: CarRepository
    private lateinit var userCarRepository: UserCarRepository
    private lateinit var mediaRepository: MediaRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val client   = Client(context)
        val tablesDB = TablesDB(client)
        val storage  = Storage(client)
        mediaRepository   = MediaRepository(db.mediaDao(), tablesDB, storage)
        carRepository     = CarRepository(tablesDB, db.carDao(), mediaRepository)
        userCarRepository = UserCarRepository(db.userCarDao(), tablesDB)
    }

    @After
    fun closeDb() = db.close()

    private suspend fun seed() {
        db.userDao().insert(User(id = USER_ID, nickname = "tester", updatedAt = NOW))
        db.brandDao().insertAll(listOf(Brand(id = BRAND_ID, name = "BMW", updatedAt = NOW)))
        db.categoryDao().insertAll(listOf(Category(id = CAT_ID, name = "Coupe", updatedAt = NOW)))
    }

    private suspend fun insertSpot(
        carId: String     = UUID.randomUUID().toString(),
        model: String     = DEFAULT_MODEL,
        year: Int         = DEFAULT_YEAR,
        price: Double     = DEFAULT_PRICE,
        notes: String     = DEFAULT_NOTES,
        mediaPath: String = DEFAULT_MEDIA_PATH,
    ): String {
        carRepository.insertCar(
            Car(
                id         = carId,
                brandId    = BRAND_ID,
                categoryId = CAT_ID,
                model      = model,
                year       = year,
                price      = price,
                isTop      = false,
                updatedAt  = NOW,
                syncState  = SyncState.PENDING_CREATE,
            )
        )
        userCarRepository.insertUserCar(
            UserCar(
                id        = UUID.randomUUID().toString(),
                userId    = USER_ID,
                carId     = carId,
                notes     = notes,
                location  = DEFAULT_LOCATION,
                updatedAt = NOW,
                syncState = SyncState.PENDING_CREATE,
            )
        )
        db.mediaDao().insert(
            Media(
                id        = UUID.randomUUID().toString(),
                carId     = carId,
                type      = MediaTypeEnum.PHOTO,
                filePath  = mediaPath,
                updatedAt = NOW,
            )
        )
        return carId
    }

    private suspend fun removeSpot(carId: String) {
        userCarRepository.softDeleteUserCar(USER_ID, carId)
        carRepository.softDeleteUserCar(carId)
    }


    @Test
    fun insertSpotPersistsCarUserCarAndMedia() = runTest {
        seed()
        val carId = insertSpot()

        val storedCar = db.carDao().getCarById(carId).first()
        assertNotNull(storedCar)
        assertEquals(SyncState.PENDING_CREATE, storedCar.syncState)
        assertEquals(DEFAULT_MODEL, storedCar.model)

        val storedUserCar = db.userCarDao().observeUserCar(USER_ID, carId).first()
        assertNotNull(storedUserCar)
        assertEquals(SyncState.PENDING_CREATE, storedUserCar.syncState)
        assertEquals(LAT, storedUserCar.location.latitude, LAT_DELTA)

        val storedMedia = db.mediaDao().getMediaByCarIdSnapshot(carId)
        assertEquals(1, storedMedia.size)
        assertEquals(MediaTypeEnum.PHOTO, storedMedia.first().type)
    }

    @Test
    fun insertedSpotAppearsInUsersGarage() = runTest {
        seed()
        val carId = insertSpot(model = "GT3")

        val garage = userCarRepository.getUserCars(USER_ID).first()
        assertEquals(1, garage.size)
        assertEquals(carId, garage.first().car.id)
        assertEquals("GT3", garage.first().car.model)
    }

    @Test
    fun insertedSpotIsVisibleAsPendingForSync() = runTest {
        seed()
        val carId = insertSpot()

        val pendingCars = db.carDao().getPendingRecords()
        assertTrue(pendingCars.any { it.id == carId && it.syncState == SyncState.PENDING_CREATE })

        val pendingUserCars = db.userCarDao().getPendingRecords()
        assertTrue(pendingUserCars.any { it.carId == carId && it.syncState == SyncState.PENDING_CREATE })
    }

    @Test
    fun insertTwoSpotsBothAppearInGarage() = runTest {
        seed()
        val first  = insertSpot(model = "M3")
        val second = insertSpot(model = "M5")

        val garage = userCarRepository.getUserCars(USER_ID).first().map { it.car.id }.toSet()
        assertEquals(setOf(first, second), garage)
    }


    @Test
    fun removeSpotMarksCarAndUserCarAsPendingDeleteForSync() = runTest {
        seed()
        val carId = insertSpot()

        removeSpot(carId)

        val pendingCars = db.carDao().getPendingRecords()
        assertTrue(pendingCars.any { it.id == carId && it.syncState == SyncState.PENDING_DELETE })

        val pendingUserCars = db.userCarDao().getPendingRecords()
        assertTrue(pendingUserCars.any { it.carId == carId && it.syncState == SyncState.PENDING_DELETE })
    }

    @Test
    fun removeSpotHidesSpotFromGarage() = runTest {
        seed()
        val carId = insertSpot()
        assertEquals(1, userCarRepository.getUserCars(USER_ID).first().size)

        removeSpot(carId)

        val garage = userCarRepository.getUserCars(USER_ID).first()
        assertTrue(garage.isEmpty(), "expected garage empty, got $garage")
    }






    @Test
    fun softDeleteCarIsNoForTopCar() = runTest {
        seed()
        val topCarId = UUID.randomUUID().toString()
        db.carDao().insert(
            Car(
                id         = topCarId,
                brandId    = BRAND_ID,
                categoryId = CAT_ID,
                model      = "911 GT3 (top)",
                year       = DEFAULT_YEAR,
                price      = 200_000.0,
                isTop      = true,
                updatedAt  = NOW,
                syncState  = SyncState.SYNCED,
            )
        )

        carRepository.softDeleteUserCar(topCarId)

        val car = db.carDao().getCarById(topCarId).first()
        assertNotNull(car)
        assertEquals(SyncState.SYNCED, car.syncState)
    }

}