package com.example.carspotter.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Media
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CarDetailRepository
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.FavouriteRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.UserDreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class GarageCarModel(
    val carId: String,
    val brandName: String,
    val category: String,
    val year: Int,
    val model: String,
    val price: Double,
    val description: String,
    val longitude: Double,
    val latitude: Double,
    val allMediaURLs: List<Media>
)

data class GarageCarState(
    val details: GarageCarModel,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GarageCarViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val carDetailRepository: CarDetailRepository,
    private val brandRepository: BrandRepository,
    private val mediaRepository: MediaRepository,
    private val categoryRepository: CategoryRepository,
    private val dreamRepository: UserDreamRepository,
    private val favouriteRepository: FavouriteRepository,
    private val accountService: AccountService,
    savedStateHandle: SavedStateHandle


): ViewModel(){

    private val userId = MutableStateFlow<String?>(null)
    init {
        viewModelScope.launch {
            userId.value = accountService.getLoggedIn()?.id
        }
    }

    private val carId: String = checkNotNull(savedStateHandle["carId"])

}