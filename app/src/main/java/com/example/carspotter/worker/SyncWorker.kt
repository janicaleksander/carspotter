package com.example.carspotter.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.carspotter.auth.AccountService
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountService: AccountService

) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val user = accountService.getLoggedIn()
            if (user == null){
                return Result.success()
            }

/*
            brandRepository.syncBrands() // -> error
            categoryRepository.syncCategories() // -> error
            userCarRepository.syncCar("id")
            favouriteRepository.syncFavourites("id")

            //TODO repair this and error handling
            userRepository.syncUser("id") // -> error
            mediaRepository.syncMedia("id")
            settingsRepository.syncSettings() // -> error
*/

            Log.e("SyncWorker", "Data sync successful")
            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing data", e)
            Result.failure()
        }
    }
}