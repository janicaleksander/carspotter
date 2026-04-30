package com.example.carspotter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.models.Category
import com.example.carspotter.repository.BrandRepository
import com.example.carspotter.repository.CarRepository
import com.example.carspotter.repository.CategoryRepository
import com.example.carspotter.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TopCarUiModel(
    val carId: String,
    val brandName: String,
    val model: String,
    val powerHP: Int,
    val acceleration: Double,
    val maxSpeed: Double,
    val imageUrl: String?,
)

data class TopsUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val topCars: List<TopCarUiModel> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TopsViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TopsUiState> = combine(
        categoryRepository.getCategories(),
        _selectedCategoryId.flatMapLatest { id ->
            if (id == null) carRepository.getTopCars()
            else carRepository.getTopCarsByCategory(id)
        },
        brandRepository.getBrands(),
    ) { categories, cars, brands ->
        Triple(categories, cars, brands)
    }.flatMapLatest { (categories, cars, brands) ->
        val carIds = cars.map { it.car.id }.distinct()
        mediaRepository.getPhotoMediaForCars(carIds).map { medias ->
            val brandMap = brands.associateBy({ it.id }, { it.name })
            val photosByCarId = medias
                .groupBy { it.carId }
                .mapValues { (_, mediaList) -> mediaList.firstOrNull()?.filePath }

            TopsUiState(
                categories = categories,
                selectedCategoryId = _selectedCategoryId.value,
                topCars = cars.map { cwd ->
                    TopCarUiModel(
                        carId = cwd.car.id,
                        brandName = brandMap[cwd.car.brandId] ?: "",
                        model = cwd.car.model,
                        powerHP = cwd.details?.powerHP ?: 0,
                        acceleration = cwd.details?.acceleration ?: 0.0,
                        maxSpeed = cwd.details?.maxSpeed ?: 0.0,
                        imageUrl = photosByCarId[cwd.car.id],
                    )
                },
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TopsUiState())

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value =
            if (_selectedCategoryId.value == categoryId) null else categoryId
    }
}

/*
*
* Bez stateIn:
kotlin// ViewModel
val state: Flow<TopsUiState> = repo.getItems()
kotlin// UI
val uiState by viewModel.state.collectAsStateWithLifecycle()
collectAsStateWithLifecycle zatrzymuje kolekcję gdy ekran schodzi w tło. Gdy wraca — zaczyna od nowa. Flow nie pamięta ostatniej wartości, UI przez chwilę nie ma stanu (migotanie, loading).

Z stateIn:
kotlinval state: StateFlow<TopsUiState> = repo.getItems()
    .stateIn(viewModelScope, WhileSubscribed(5000), TopsUiState())
StateFlow zawsze trzyma ostatnią wartość. Gdy UI wraca — dostaje ją natychmiast, zero migotania.

Obrazowo:
Flow       = rura z wodą — odkręcasz, leci; zakręcasz, stop; odkręcasz znowu — czekasz
StateFlow  = zbiornik     — zawsze pełny, odkręcasz i masz wodę od razu

Wielu odbiorców to edge case (np. dwa ekrany obserwują ten sam stan). W praktyce stateIn używasz głównie dla natychmiastowej wartości przy powrocie na ekran.
*
* */
