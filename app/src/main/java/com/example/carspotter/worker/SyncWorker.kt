package com.example.carspotter.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CategoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            brandRepository.syncBrands()
            categoryRepository.syncCategories()
            Result.success()

        } catch (e: Exception) {
            Result.failure()
        }
    }
}