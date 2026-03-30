package com.example.carspotter.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.carspotter.auth.AccountService
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.SettingsRepository
import com.example.carspotter.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val carRepository: CarRepository,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val user = accountService.getLoggedIn()
            if (user == null){
                return Result.success()
            }
            categoryRepository.syncCategories()
            brandRepository.syncBrands()
            userRepository.syncUser(user.id) // -> error
            carRepository.syncCar(user.id)
            settingsRepository.syncSettings()

            Log.e("SyncWorker", "Data sync successful")
            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing data", e)
            Result.failure()
        }
    }
}