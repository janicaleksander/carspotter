package com.example.carspotter.repository

import com.example.carspotter.dao.MediaDao
import com.example.carspotter.models.Media
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val mediaDao: MediaDao

) {
        fun getMediaForCar(carId: String): Flow<List<Media>>{
            return mediaDao.getMediaByCarId(carId)
        }
}