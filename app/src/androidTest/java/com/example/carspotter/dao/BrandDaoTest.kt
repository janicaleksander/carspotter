package com.example.carspotter.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carspotter.database.AppDatabase
import com.example.carspotter.models.Brand
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BrandDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var brandDao: BrandDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        brandDao = db.brandDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertAndRetrieveInsertedBrand() = runTest {
        val bid = UUID.randomUUID().toString();
        val brand = Brand(
            id = bid,
            name = "Porsche",
            updatedAt = LocalDateTime.of(2026, 5, 15, 12, 0),
        )

        brandDao.insertAll(listOf(brand))

        val fromDb = brandDao.getById(brand.id).first()
        assertNotNull(fromDb)
        assertEquals(brand.name, fromDb?.name)
    }

    @Test
    fun getAllBrandSnapshot() = runTest {
        val brands = listOf(
            Brand(UUID.randomUUID().toString(), "BMW",     LocalDateTime.of(2026, 5, 15, 0, 0)),
            Brand(UUID.randomUUID().toString(), "Audi",    LocalDateTime.of(2026, 5, 15, 0, 0)),
            Brand(UUID.randomUUID().toString(), "Porsche", LocalDateTime.of(2026, 5, 15, 0, 0)),
        )

        brandDao.insertAll(brands)

        val snapshot = brandDao.getAllSnapshot()
        assertEquals(3, snapshot.size)
        assertEquals(setOf("BMW", "Audi", "Porsche"), snapshot.map { it.name }.toSet())
    }

    @Test
    fun insertAll_withDuplicateId_upserts() = runTest {
        val sharedId = UUID.randomUUID().toString()
        val original = Brand(sharedId, "BMW",   LocalDateTime.of(2024, 1, 1, 0, 0))
        val updated  = Brand(sharedId, "BMW M", LocalDateTime.of(2024, 6, 1, 0, 0))

        brandDao.insertAll(listOf(original))
        brandDao.insertAll(listOf(updated))

        val snapshot = brandDao.getAllSnapshot()
        assertEquals(1, snapshot.size)
        assertEquals("BMW M", snapshot.first().name)
    }

    @Test
    fun getByUnknownId() = runTest {
        assertNull(brandDao.getById(UUID.randomUUID().toString()).first())
    }
}