package com.example.carspotter.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.FavouriteRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.SettingsRepository
import com.example.carspotter.repository.UserCarRepository
import com.example.carspotter.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val userCarRepository: UserCarRepository,
    private val settingsRepository: SettingsRepository,
    private val favouriteRepository: FavouriteRepository,
    private val userRepository: UserRepository,
    private val mediaRepository: MediaRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            brandRepository.syncBrands()
            categoryRepository.syncCategories()
            userCarRepository.syncCar("id")
            favouriteRepository.syncFavourites("id")
            userRepository.syncUser("id")
            mediaRepository.syncMedia("id")
            settingsRepository.syncSettings()

            Log.e("SyncWorker", "Data sync successful")
            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing data", e)
            Result.failure()
        }
    }
}