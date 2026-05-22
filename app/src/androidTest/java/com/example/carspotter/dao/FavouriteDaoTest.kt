package com.example.carspotter.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carspotter.database.AppDatabase
import com.example.carspotter.models.Brand
import com.example.carspotter.models.Car
import com.example.carspotter.models.Category
import com.example.carspotter.models.Favourite
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.User
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class FavouriteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FavouriteDao

    private val now: LocalDateTime = LocalDateTime.of(2026, 5, 15, 12, 0)
    private val userId   = UUID.randomUUID().toString()
    private val brandId  = UUID.randomUUID().toString()
    private val catId    = UUID.randomUUID().toString()
    private val carId    = UUID.randomUUID().toString()
    private val otherCar = UUID.randomUUID().toString()


    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        dao = db.favouriteDao()

    }

    private suspend fun seed() {
        db.userDao().insert(User(id = userId, nickname = "tester", updatedAt = now))
        db.brandDao().insertAll(listOf(Brand(id = brandId, name = "BMW", updatedAt = now)))
        db.categoryDao().insertAll(listOf(Category(id = catId, name = "Coupe", updatedAt = now)))
        db.carDao().insert(
            Car(
                id = carId,
                brandId = brandId,
                categoryId = catId,
                model = "M3",
                year = 2024,
                price = 90_000.0,
                isTop = false,
                updatedAt = now,
            )
        )
        db.carDao().insert(
            Car(
                id = otherCar,
                brandId = brandId,
                categoryId = catId,
                model = "M5",
                year = 2024,
                price = 120_000.0,
                isTop = false,
                updatedAt = now,
            )
        )
    }
    @After
    fun closeDb() = db.close()


    @Test
    fun insertAndFindByUserAndCar() = runTest {
        seed()
        val fid = UUID.randomUUID().toString()
        val fav = Favourite(
            id = fid,
            userId = userId,
            carId = carId,
            syncState = SyncState.SYNCED,
            updatedAt = now,
        )
        dao.insert(fav)

        val found = dao.findByUserAndCar(userId, carId)
        assertNotNull(found)
        assertEquals(fid, found?.id)
    }

    @Test
    fun getAll() = runTest {
        seed()
        val fid1 = UUID.randomUUID().toString()
        val fid2 = UUID.randomUUID().toString()
        dao.insertAll(
            listOf(
                Favourite(fid1, userId, carId,    SyncState.SYNCED, now),
                Favourite(fid2, userId, otherCar, SyncState.SYNCED, now),
            )
        )

        val visible = dao.getAll(userId).first()
        assertEquals(2, visible.size)
        assertEquals(setOf(fid1, fid2), visible.map { it.id }.toSet())
    }

    @Test
    fun getSyncedForUser() = runTest {
        seed()
        val fid1 = UUID.randomUUID().toString()
        val fid2 = UUID.randomUUID().toString()

        dao.insertAll(
            listOf(
                Favourite(fid1, userId, carId,    SyncState.SYNCED,         now),
                Favourite(fid2, userId, otherCar, SyncState.PENDING_CREATE, now),
            )
        )

        val synced = dao.getSyncedForUser(userId)
        assertEquals(1, synced.size)
        assertEquals(fid1, synced.first().id)
    }

    @Test
    fun getPendingRecords() = runTest {
        seed()
        val fid1 = UUID.randomUUID().toString()
        val fid2 = UUID.randomUUID().toString()
        dao.insertAll(
            listOf(
                Favourite(fid1, userId, carId,    SyncState.SYNCED,         now),
                Favourite(fid2, userId, otherCar, SyncState.PENDING_CREATE, now),
            )
        )

        val pending = dao.getPendingRecords()
        assertEquals(1, pending.size)
        assertEquals(fid2, pending.first().id)
    }

    @Test
    fun hardDeleteRemovesRowEntirely() = runTest {
        seed()
        val favId = UUID.randomUUID().toString()
        dao.insert(Favourite(favId, userId, carId, SyncState.SYNCED, now))
        dao.hardDelete(favId)

        assertNull(dao.findByUserAndCar(userId, carId))
    }

    @Test
    fun observeIsFavourite() = runTest {
        seed()
        assertFalse(dao.observeIsFavourite(userId, carId).first())

        val favId = UUID.randomUUID().toString()
        dao.insert(Favourite(favId, userId, carId, SyncState.SYNCED, now))
        assertTrue(dao.observeIsFavourite(userId, carId).first())

    }

    @Test
    fun findByUserAndCar() = runTest {
        seed()
        assertNull(dao.findByUserAndCar(userId, UUID.randomUUID().toString()))
    }

}
