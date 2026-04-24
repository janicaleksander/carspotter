package com.example.carspotter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Brand
import com.example.carspotter.models.Car
import com.example.carspotter.models.Category
import com.example.carspotter.models.MediaTypeEnum
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

data class GarageCarUiModel(
    val carId: String,
    val brandName: String,
    val model: String,
    val category: String,
    val isFavorite: Boolean,
    val imageUrl: String?,
)

//default is to show ALL user car
//sort by price


//only one photo type of media can be here

data class GarageUiState(
    val categories: List<Category> = emptyList(),
    val brands : List<Brand> = emptyList(),
    val userCars : List<GarageCarUiModel> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedBrandId: String? = null,
    val isSelectedFavorites: Boolean = false,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GarageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val userCarRepository: UserCarRepository,
    private val mediaRepository: MediaRepository,
    private val favouriteRepository: FavouriteRepository,
    private val accountService: AccountService
): ViewModel() {
    private val userId = MutableStateFlow<String?>(null)
    init {
        viewModelScope.launch {
            userId.value = accountService.getLoggedIn()?.id
        }
    }
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _selectedBrandId = MutableStateFlow<String?>(null)
    private val _isSelectedFavorites = MutableStateFlow(false)

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
        _selectedBrandId.value = null
        _isSelectedFavorites.value = false
    }
    fun selectBrand(brandId: String?) {
        _selectedBrandId.value = brandId
        _selectedCategoryId.value = null
        _isSelectedFavorites.value = false
    }
    fun isSelectedFavourite() {
        _isSelectedFavorites.value = !_isSelectedFavorites.value
        _selectedBrandId.value = null
        _selectedCategoryId.value = null
    }

    fun onHeartClick(carId: String) {//toogle favorite status also in room db and appwrite (so sync)
       //todo
    }

        val uiState:StateFlow<GarageUiState> = combine(
            categoryRepository.getCategories(),
            brandRepository.getBrands(),
            mediaRepository.getMediaForCar(userId.value ?: ""),//TOOD REPAIR BECAUSE NOW THIS IS NOW IT IS USER ID BUT WE HAVE TO GET HERE CAR ID
            userId.flatMapLatest { id ->
                if (id == null) flowOf()
                else userCarRepository.getUserCars(id)

            },
        ) { categories, brands,media, userCars ->
            GarageUiState(
                categories = categories,
                brands = brands,
                userCars = userCars.map { uc ->
                    val carMedia = media.firstOrNull { it.carId == uc.car.id && it.type == MediaTypeEnum.PHOTO }?.filePath
                    GarageCarUiModel(
                        carId = uc.car.id,
                        brandName = brands.firstOrNull { it.id == uc.car.brandId }?.name ?: "",
                        model = uc.car.model,
                        category = categories.firstOrNull { it.id == uc.car.categoryId }?.name ?: "",
                        isFavorite =favouriteRepository.observeIsFavourite(userId.value ?: "", uc.car.id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false).value,
                        imageUrl = carMedia
                    )
                },
                selectedCategoryId = _selectedCategoryId.value,
                selectedBrandId = _selectedBrandId.value,
                isSelectedFavorites = _isSelectedFavorites.value,
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GarageUiState())

}