package com.example.carspotter.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Category
import com.example.carspotter.models.Media
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CarDetailRepository
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.UserDreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class DetailTopCarModel(
    val carId: String,
    val brandName: String,
    val category: String,
    val year: Int,
    val model: String,
    val powerHP: Int,
    val acceleration: Double,
    val description: String,
    val maxSpeed: Double,
    val allMediaURLs: List<Media>
)


data class DetailTopCarState(
    val details: DetailTopCarModel,
    val isLoading: Boolean = false
)
//TODO poczytac czemu tak bo problem blokay UI inaczje, ze trzeba launche itd i suspendy

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TopsDetailViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val carDetailRepository: CarDetailRepository,
    private val brandRepository: BrandRepository,
    private val mediaRepository: MediaRepository,
    private val categoryRepository: CategoryRepository,
    private val dreamRepository: UserDreamRepository,
    private val accountService: AccountService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // userId pojawia sie asynchronicznie po starcie ekranu
    private val userId = MutableStateFlow<String?>(null)
    init {
        viewModelScope.launch {
            userId.value = accountService.getLoggedIn()?.id
        }
    }

    private val carId: String = checkNotNull(savedStateHandle["carId"])


    // Obserwujemy dream dopiero, gdy znamy userId.
    val isDream : StateFlow<Boolean> = userId
        .filterNotNull()
        .flatMapLatest { currentUserId -> dreamRepository.observeIsDream(currentUserId, carId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    //jak to sie dzieje ze jak zrobie add to ta obserwacja sie zmieni, jak to obserwuje?

    fun addUserDream(carID: String) {
        viewModelScope.launch {
            val currentUserId = userId.value ?: return@launch //TODO po co to
            dreamRepository.addUserDream(currentUserId, carID)
            dreamRepository.pushPending()
        }
    }

    fun deleteUserDream(carID: String) {
        viewModelScope.launch {
            val currentUserId = userId.value ?: return@launch
            dreamRepository.deleteUserDream(currentUserId, carID)
            dreamRepository.pushPending()

        }
    }

    val uiState: StateFlow<DetailTopCarState> = carRepository.getTopCarById(carId)
        .filterNotNull() // KLUCZ 1: Przepuszcza tylko, gdy auto istnieje. Poniżej car_details na pewno NIE JEST nullem.
        .flatMapLatest { car_details ->
            combine(
                brandRepository.getBrandById(car_details.car.brandId)
                    .filterNotNull(), // Odfiltrowujemy null
                categoryRepository.getCategoryById(car_details.car.categoryId)
                    .filterNotNull(), // Odfiltrowujemy null
                mediaRepository.getMediaForCar(carId)
            ) { brand, category, media ->
                DetailTopCarState(
                    details = DetailTopCarModel(
                        carId = carId,
                        brandName = brand.name,
                        category = category.name,
                        year = car_details.car.year,
                        model = car_details.car.model,
                        powerHP = car_details.details?.powerHP ?: 0, // Bezpieczna wartość domyślna
                        acceleration = car_details.details?.acceleration
                            ?: 0.0, // Bezpieczna wartość domyślna
                        description = car_details.details?.description
                            ?: "", // Bezpieczna wartość domyślna,
                        maxSpeed = car_details.details?.maxSpeed ?: 0.0,
                        allMediaURLs = media
                    ),
                    isLoading = false
                )

            }

        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DetailTopCarState(
                details = DetailTopCarModel(
                    carId = "",
                    brandName = "",
                    category = "",
                    year = 0,
                    model = "",
                    powerHP = 0,
                    acceleration = 0.0,
                    description = "",
                    maxSpeed = 0.0,
                    allMediaURLs = emptyList()
                ),
                isLoading = true
            )
        )
}
//TOOD refactor thuis to loading istead od state in default