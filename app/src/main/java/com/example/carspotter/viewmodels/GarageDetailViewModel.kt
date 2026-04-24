package com.example.carspotter.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Location
import com.example.carspotter.models.Media
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.FavouriteRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.UserCarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GarageCarDetails(
    val carId: String,
    val brandName: String,
    val category: String,
    val model: String,
    val year: Int,
    val price: Double,
    val notes: String,
    val location: Location?,
    val allMediaURLs: List<Media>,
)

data class GarageDetailState(
    val details: GarageCarDetails,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GarageDetailViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository,
    private val mediaRepository: MediaRepository,
    private val userCarRepository: UserCarRepository,
    private val favouriteRepository: FavouriteRepository,
    private val accountService: AccountService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val userId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            userId.value = accountService.getLoggedIn()?.id
        }
    }

    private val carId: String = checkNotNull(savedStateHandle["carId"])

    private val _isDeleted = MutableStateFlow(false)

    val isFavourite: StateFlow<Boolean> = userId
        .filterNotNull()
        .flatMapLatest { uid -> favouriteRepository.observeIsFavourite(uid, carId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleFavourite() {
        viewModelScope.launch {
            val uid = userId.value ?: return@launch
            favouriteRepository.toggleFavourite(uid, carId)
            favouriteRepository.pushPending()
        }
    }

    fun removeFromGarage() {
        viewModelScope.launch {
            val uid = userId.value ?: return@launch
            // Order matters: delete the user_car join row first so Appwrite's
            // car delete doesn't trip over a dangling reference on push.
            userCarRepository.softDeleteUserCar(uid, carId)
            carRepository.softDeleteUserCar(carId)
            userCarRepository.pushPending()
            carRepository.pushPending()
            _isDeleted.value = true
        }
    }

    val uiState: StateFlow<GarageDetailState> = userId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(defaultLoadingState())
            else carRepository.getCarById(carId)
                .filterNotNull()
                .flatMapLatest { car ->
                    combine(
                        brandRepository.getBrandById(car.brandId).filterNotNull(),
                        categoryRepository.getCategoryById(car.categoryId).filterNotNull(),
                        mediaRepository.getMediaForCar(carId),
                        userCarRepository.observeUserCar(uid, carId),
                    ) { brand, category, media, userCar ->
                        GarageDetailState(
                            details = GarageCarDetails(
                                carId = car.id,
                                brandName = brand.name,
                                category = category.name,
                                model = car.model,
                                year = car.year,
                                price = car.price,
                                notes = userCar?.notes.orEmpty(),
                                location = userCar?.location,
                                allMediaURLs = media,
                            ),
                            isLoading = false,
                        )
                    }
                }
        }
        .combine(_isDeleted) { state, deleted -> state.copy(isDeleted = deleted) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultLoadingState())

    private fun defaultLoadingState() = GarageDetailState(
        details = GarageCarDetails(
            carId = "",
            brandName = "",
            category = "",
            model = "",
            year = 0,
            price = 0.0,
            notes = "",
            location = null,
            allMediaURLs = emptyList(),
        ),
        isLoading = true,
    )
}
