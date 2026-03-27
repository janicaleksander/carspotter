package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.CarDao
import com.example.carspotter.dao.UserCarDao
import com.example.carspotter.models.Car
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepository  @Inject constructor(
    private val carDao: CarDao,
    private val userCarDao: UserCarDao,
    private val tablesDB: TablesDB
){
    fun getCars(): Flow<List<Car>> {
        return carDao.getAll()
    }

}