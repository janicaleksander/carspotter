package com.example.carspotter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Brand
import com.example.carspotter.models.CarWithDetails
import com.example.carspotter.models.Category
import com.example.carspotter.models.Favourite
import com.example.carspotter.models.Media
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserCarInfo
import com.example.carspotter.models.UserDream
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.FavouriteRepository
import com.example.carspotter.repository.MediaRepository
import com.example.carspotter.repository.UserCarRepository
import com.example.carspotter.repository.UserDreamRepository
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

data class HomeCarUiModel(
    val carId: String,
    val brandName: String,
    val category: String,
    val imageUrl: String?,
    val isFavourite: Boolean,
    val isDream: Boolean,
)

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val filterMode: HomeFilterMode = HomeFilterMode.ALL,
    val browseMode: HomeBrowseMode = HomeBrowseMode.RECENT,
    val homeCars: List<HomeCarUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val totalCarCount: Int = 0,
)

enum class HomeFilterMode { ALL, FAVOURITES }
enum class HomeBrowseMode { RECENT, DREAMS }

private data class HomeFilters(
    val searchQuery: String,
    val categoryId: String?,
    val mode: HomeFilterMode,
    val browseMode: HomeBrowseMode,
)

private data class HomeData(
    val categories: List<Category>,
    val brands: List<Brand>,
    val userCars: List<UserCarInfo>,
    val dreamCars: List<CarWithDetails>,
    val favourites: List<Favourite>,
    val medias: List<Media>,
)
@OptIn(ExperimentalCoroutinesApi::class)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userDreamRepository: UserDreamRepository,
    private val userCarRepository: UserCarRepository,
    private val favouriteRepository: FavouriteRepository,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val mediaRepository: MediaRepository,
    private val carRepository: CarRepository,
) : ViewModel() {

    private val userId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            userId.value = accountService.getLoggedIn()?.id
        }
    }

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _filterMode = MutableStateFlow(HomeFilterMode.ALL)
    private val _browseMode = MutableStateFlow(HomeBrowseMode.RECENT)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun selectFilterMode(mode: HomeFilterMode) {
        _filterMode.value = mode
    }

    fun selectBrowseMode(showDreams: Boolean) {
        _browseMode.value = if (showDreams) HomeBrowseMode.DREAMS else HomeBrowseMode.RECENT
        if (showDreams) {
            _filterMode.value = HomeFilterMode.ALL
        }
    }

    fun onHeartClick(carId: String) {
        viewModelScope.launch {
            val uid = userId.value ?: return@launch
            favouriteRepository.toggleFavourite(uid, carId)
            favouriteRepository.pushPending()
        }
    }

    private val dataFlow: Flow<HomeData?> = userId.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(null)
        } else {
            combine(
                categoryRepository.getCategories(),
                brandRepository.getBrands(),
                userDreamRepository.getCarsFromDreams(uid),
                userCarRepository.getUserCars(uid),
                favouriteRepository.getFavourites(uid),
            ) { categories, brands, userDreams, userCars, favourites ->
                Quintuple(
                    categories = categories,
                    brands = brands,
                    userDreams = userDreams,
                    userCars = userCars,
                    favourites = favourites,
                )
            }.combine(carRepository.getTopCars()) { base, topCars ->
                val activeDreams = base.userDreams
                    .filter { it.syncState != SyncState.PENDING_DELETE }
                    .sortedByDescending(UserDream::updatedAt)
                val dreamRank = activeDreams
                    .mapIndexed { index, dream -> dream.carId to index }
                    .toMap()
                val dreamCars = topCars
                    .filter { it.car.id in dreamRank }
                    .sortedBy { dreamRank[it.car.id] ?: Int.MAX_VALUE }

                HomeData(
                    categories = base.categories,
                    brands = base.brands,
                    userCars = base.userCars,
                    dreamCars = dreamCars,
                    favourites = base.favourites,
                    medias = emptyList(),
                )
            }.flatMapLatest { base ->
                val carIds = (
                    base.userCars.map { it.car.id } +
                        base.dreamCars.map { it.car.id }
                    ).distinct()
                mediaRepository.getPhotoMediaForCars(carIds).map { medias ->
                    base.copy(medias = medias)
                }
            }
        }
    }

    private val filtersFlow: Flow<HomeFilters> = combine(
        _searchQuery,
        _selectedCategoryId,
        _filterMode,
        _browseMode,
    ) { searchQuery, categoryId, mode, browseMode ->
        HomeFilters(searchQuery, categoryId, mode, browseMode)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        dataFlow,
        filtersFlow,
    ) { data, filters ->
        if (data == null) HomeUiState(isLoading = true)
        else buildUiState(data, filters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    private fun buildUiState(data: HomeData, filters: HomeFilters): HomeUiState {
        val brandMap = data.brands.associateBy({ it.id }, { it.name })
        val categoryMap = data.categories.associateBy({ it.id }, { it.name })
        val photoByCarId: Map<String, String?> = data.medias
            .groupBy { it.carId }
            .mapValues { (_, list) -> list.firstOrNull()?.filePath }

        val favouriteCarIds: Set<String> = data.favourites
            .filter { it.syncState != SyncState.PENDING_DELETE }
            .map { it.carId }
            .toSet()

        fun matchesSearch(brandName: String): Boolean {
            val query = filters.searchQuery.trim()
            if (query.isEmpty()) return true
            val normalized = query.lowercase()
            return brandName.lowercase().contains(normalized)
        }

        val homeCars = if (filters.browseMode == HomeBrowseMode.DREAMS) {
            data.dreamCars
                .asSequence()
                .filter { filters.categoryId == null || it.car.categoryId == filters.categoryId }
                .map { carWithDetails ->
                    val brandName = brandMap[carWithDetails.car.brandId].orEmpty()
                    Triple(carWithDetails, brandName, categoryMap[carWithDetails.car.categoryId].orEmpty())
                }
                .filter { (_, brandName, _) -> matchesSearch(brandName) }
                .map { (carWithDetails, brandName, categoryName) ->
                    HomeCarUiModel(
                        carId = carWithDetails.car.id,
                        brandName = brandName,
                        category = categoryName,
                        imageUrl = photoByCarId[carWithDetails.car.id],
                        isFavourite = false,
                        isDream = true,
                    )
                }
                .toList()
        } else {
            data.userCars
                .asSequence()
                .filter { filters.categoryId == null || it.car.categoryId == filters.categoryId }
                .filter { filters.mode != HomeFilterMode.FAVOURITES || it.car.id in favouriteCarIds }
                .map { userCar ->
                    val brandName = brandMap[userCar.car.brandId].orEmpty()
                    Triple(userCar, brandName, categoryMap[userCar.car.categoryId].orEmpty())
                }
                .filter { (_, brandName, _) -> matchesSearch(brandName) }
                .map { (userCar, brandName, categoryName) ->
                    HomeCarUiModel(
                        carId = userCar.car.id,
                        brandName = brandName,
                        category = categoryName,
                        imageUrl = photoByCarId[userCar.car.id],
                        isFavourite = userCar.car.id in favouriteCarIds,
                        isDream = false,
                    )
                }
                .toList()
        }

        val totalCarCount = if (filters.browseMode == HomeBrowseMode.DREAMS) data.dreamCars.size else data.userCars.size

        return HomeUiState(
            categories = data.categories,
            searchQuery = filters.searchQuery,
            selectedCategoryId = filters.categoryId,
            filterMode = filters.mode,
            browseMode = filters.browseMode,
            homeCars = homeCars,
            isLoading = false,
            totalCarCount = totalCarCount,
        )
    }

    private data class Quintuple(
        val categories: List<Category>,
        val brands: List<Brand>,
        val userDreams: List<UserDream>,
        val userCars: List<UserCarInfo>,
        val favourites: List<Favourite>,
    )
}
