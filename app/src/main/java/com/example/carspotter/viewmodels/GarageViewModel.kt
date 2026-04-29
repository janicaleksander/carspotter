package com.example.carspotter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Brand
import com.example.carspotter.models.Category
import com.example.carspotter.models.Favourite
import com.example.carspotter.models.Media
import com.example.carspotter.models.MediaTypeEnum
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserCarInfo
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.FavouriteRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.UserCarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GarageCarUiModel(
    val carId: String,
    val brandName: String,
    val model: String,
    val category: String,
    val year: Int,
    val price: Double,
    val isFavourite: Boolean,
    val imageUrl: String?,
)

enum class GarageFilterMode { ALL, FAVOURITES }

enum class SortOption { NONE, PRICE_ASC, PRICE_DESC }

data class GarageUiState(
    val categories: List<Category> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val userCars: List<GarageCarUiModel> = emptyList(),
    /** Cars in garage before category/brand/favourites filters (sort not applied). */
    val totalGarageCarCount: Int = 0,
    val selectedCategoryId: String? = null,
    val selectedBrandId: String? = null,
    val filterMode: GarageFilterMode = GarageFilterMode.ALL,
    val sortOption: SortOption = SortOption.NONE,
    val isLoading: Boolean = true,
)

private data class GarageData(
    val categories: List<Category>,
    val brands: List<Brand>,
    val userCars: List<UserCarInfo>,
    val favourites: List<Favourite>,
    val medias: List<Media>,
)

private data class GarageFilters(
    val categoryId: String?,
    val brandId: String?,
    val mode: GarageFilterMode,
    val sort: SortOption,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GarageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val userCarRepository: UserCarRepository,
    private val mediaRepository: MediaRepository,
    private val favouriteRepository: FavouriteRepository,
    private val accountService: AccountService,
) : ViewModel() {

    private val userId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            userId.value = accountService.getLoggedIn()?.id
        }
    }

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _selectedBrandId = MutableStateFlow<String?>(null)
    private val _filterMode = MutableStateFlow(GarageFilterMode.ALL)
    private val _sortOption = MutableStateFlow(SortOption.NONE)

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun selectBrand(brandId: String?) {
        _selectedBrandId.value = brandId
    }

    fun selectFilterMode(mode: GarageFilterMode) {
        _filterMode.value = mode
    }

    fun selectSort(sort: SortOption) {
        _sortOption.value = sort
    }

    fun onHeartClick(carId: String) {
        viewModelScope.launch {
            val uid = userId.value ?: return@launch
            favouriteRepository.toggleFavourite(uid, carId)
            favouriteRepository.pushPending()
        }
    }

    private val dataFlow: Flow<GarageData?> = userId.flatMapLatest { uid ->
        if (uid == null) flowOf(null)
        else combine(
            categoryRepository.getCategories(),
            brandRepository.getBrands(),
            userCarRepository.getUserCars(uid),
            favouriteRepository.getFavourites(uid),
        ) { categories, brands, userCars, favourites ->
            GarageData(
                categories = categories,
                brands = brands,
                userCars = userCars,
                favourites = favourites,
                medias = emptyList(),
            )
        }.flatMapLatest { base ->
            val carIds = base.userCars.map { it.car.id }.distinct()
            mediaRepository.getPhotoMediaForCars(carIds).map { medias ->
                base.copy(medias = medias)
            }
        }
    }

    private val filtersFlow: Flow<GarageFilters> = combine(
        _selectedCategoryId,
        _selectedBrandId,
        _filterMode,
        _sortOption,
    ) { categoryId, brandId, mode, sort ->
        GarageFilters(categoryId, brandId, mode, sort)
    }

    val uiState: StateFlow<GarageUiState> = combine(
        dataFlow,
        filtersFlow,
    ) { data, filters ->
        if (data == null) GarageUiState(isLoading = true)
        else buildUiState(data, filters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GarageUiState())

    private fun buildUiState(data: GarageData, filters: GarageFilters): GarageUiState {
        val brandMap = data.brands.associateBy({ it.id }, { it.name })
        val categoryMap = data.categories.associateBy({ it.id }, { it.name })
        val photoByCarId: Map<String, String?> = data.medias
            .groupBy { it.carId }
            .mapValues { (_, list) -> list.firstOrNull()?.filePath }
        val favouriteCarIds: Set<String> = data.favourites
            .filter { it.syncState != SyncState.PENDING_DELETE }
            .map { it.carId }
            .toSet()

        val filteredCars = data.userCars
            .asSequence()
            .filter { filters.categoryId == null || it.car.categoryId == filters.categoryId }
            .filter { filters.brandId == null || it.car.brandId == filters.brandId }
            .filter { filters.mode != GarageFilterMode.FAVOURITES || it.car.id in favouriteCarIds }
            .toList()

        val sortedCars = when (filters.sort) {
            SortOption.NONE -> filteredCars
            SortOption.PRICE_ASC -> filteredCars.sortedBy { it.car.price }
            SortOption.PRICE_DESC -> filteredCars.sortedByDescending { it.car.price }
        }

        val uiCars = sortedCars.map { uc ->
            GarageCarUiModel(
                carId = uc.car.id,
                brandName = brandMap[uc.car.brandId].orEmpty(),
                model = uc.car.model,
                category = categoryMap[uc.car.categoryId].orEmpty(),
                year = uc.car.year,
                price = uc.car.price,
                isFavourite = uc.car.id in favouriteCarIds,
                imageUrl = photoByCarId[uc.car.id],
            )
        }

        return GarageUiState(
            categories = data.categories,
            brands = data.brands,
            userCars = uiCars,
            totalGarageCarCount = data.userCars.size,
            selectedCategoryId = filters.categoryId,
            selectedBrandId = filters.brandId,
            filterMode = filters.mode,
            sortOption = filters.sort,
            isLoading = false,
        )
    }
}
