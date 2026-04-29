package com.example.carspotter.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Car
import com.example.carspotter.models.Location
import com.example.carspotter.models.Media
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserCar
import com.example.carspotter.network_monitor.NetworkMonitor
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.UserCarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class NewSpotModel(
    val brandId: String,
    val categoryId: String,
    val model: String,
    val year: Int,
    val price: Double,
    val location: Pair<Double, Double>, // latitude and longitude
    val notes: String,
    val media: List<Media> = emptyList(), // local files picked by the user
)

@HiltViewModel
class NewSpotViewModel @Inject constructor(
    private val accountService: AccountService,
    private val networkMonitor: NetworkMonitor,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository,
    private val userCarRepository: UserCarRepository,
    private val carRepository: CarRepository,
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    private val userId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            userId.value = accountService.getLoggedIn()?.id
        }
    }

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Persists a new spot.
     *
     * Car + UserCar follow the offline-first pattern (Room first, then push).
     * Media is intentionally online-only — the UI gates the save button on
     * [isOnline], so we can upload files straight to Appwrite Storage and
     * persist the resulting URLs in Room.
     */
    fun saveSpot(userID: String, newSpot: NewSpotModel) {
        viewModelScope.launch {
            try {
                val carId = ID.unique()
                mediaRepository.uploadAndSaveMedia(carId, newSpot.media)
                carRepository.insertCar(
                    Car(
                        id = carId,
                        brandId = newSpot.brandId,
                        categoryId = newSpot.categoryId,
                        model = newSpot.model,
                        year = newSpot.year,
                        price = newSpot.price,
                        isTop = false,
                        updatedAt = LocalDateTime.now(),
                        syncState = SyncState.PENDING_CREATE,
                    )
                )
                carRepository.pushPending()

                userCarRepository.insertUserCar(
                    UserCar(
                        userId = userID,
                        carId = carId,
                        notes = newSpot.notes,
                        location = Location(
                            latitude = newSpot.location.first,
                            longitude = newSpot.location.second,
                        ),
                        updatedAt = LocalDateTime.now(),
                        syncState = SyncState.PENDING_CREATE,
                    )
                )
                userCarRepository.pushPending()


            } catch (e: Exception) {
                Log.e("NewSpotViewModel", "Failed to save spot", e)
            }
        }
    }
}
