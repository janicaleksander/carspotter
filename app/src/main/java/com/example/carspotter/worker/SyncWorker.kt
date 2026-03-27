package com.example.carspotter.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.UserCarRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val userCarRepository: UserCarRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            brandRepository.syncBrands()
            categoryRepository.syncCategories()
            userCarRepository.syncCar("id")

            Log.e("SyncWorker", "Data sync successful")
            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing data", e)
            Result.failure()
        }
    }
}