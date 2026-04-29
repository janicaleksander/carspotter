package com.example.carspotter.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.carspotter.auth.AccountService
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CarDetailRepository
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.FavouriteRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.SettingsRepository
import com.example.carspotter.repository.UserCarRepository
import com.example.carspotter.repository.UserDreamRepository
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
    private val settingsRepository: SettingsRepository,
    private val userCarRepository: UserCarRepository,
    private val userDreamRepository: UserDreamRepository,
    private val carDetailRepository: CarDetailRepository,
    private val mediaRepository: MediaRepository,
    private val favouriteRepository: FavouriteRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val user = accountService.getLoggedIn()
            if (user == null) {
                return Result.success()
            }
            carRepository.pushPending() // ← dodajesz
            userCarRepository.pushPending()
            favouriteRepository.pushPending()
            userDreamRepository.pushPending()//TODO to!!!! ->dodaje do rom i push pedning
            // 1. Global lookup tables (no FK dependencies between them)
            val categoryIds = categoryRepository.syncCategories()
            val brandIds = brandRepository.syncBrands()
            userRepository.syncUser(user.id)

            // 2. Fetch user relationships from cloud (don't insert yet — cars not in Room)
            val userCars = userCarRepository.fetchFromCloud(user.id)
            val userDreams = userDreamRepository.fetchFromCloud(user.id)

            // 3. Sync cars to Room (FK satisfied: brand + category exist)
            val userCarIds = userCars.map { it.carId }.distinct()
            val result = carRepository.syncCars(userCarIds)
            categoryRepository.pruneRemovedCategories(categoryIds)
            brandRepository.pruneRemovedBrands(brandIds)

            // 4. Insert user relationships (cars now exist in Room)
            userCarRepository.saveToRoom(user.id, userCars)
            userDreamRepository.saveToRoom(user.id, userDreams)

            // 5. Sync dependent data (cars exist in Room)
            carDetailRepository.syncCarDetails(result.topCarIds)
            mediaRepository.syncMedia(result.allCarIds)
            favouriteRepository.syncFavourites(user.id)

            settingsRepository.syncSettings()

            Log.e("SyncWorker", "Data sync successful")
            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing data", e)
            Result.failure()
        }
    }
}
