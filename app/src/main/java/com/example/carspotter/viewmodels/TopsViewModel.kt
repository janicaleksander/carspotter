package com.example.carspotter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.models.Category
import com.example.carspotter.models.Media
import com.example.carspotter.models.MediaTypeEnum
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
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TopCarUiModel(
    val carId: String,
    val brandName: String,
    val category: String,
    val year:Int,
    val model: String,
    val powerHP: Int,
    val acceleration: Double,
    val maxSpeed: Double,
    val imageUrl: String?,
    val allMediaURLs:  Map<String, List<Media>>
)

data class TopsUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val topCars: List<TopCarUiModel> = emptyList(),
    val isLoading: Boolean = true
)

//todo jeslo nie ma zdjec to nie pokazujemy tego kfelka
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
        mediaRepository.getAllMedia()
    ) { categories, cars, brands, medias ->
        val brandMap = brands.associateBy({ it.id }, { it.name })
        val photosByCarId = medias.groupBy { it.carId }.mapValues { (_,mediaList) ->  mediaList.firstOrNull{it.type == MediaTypeEnum.PHOTO }?.filePath}
        val carMedia = medias.groupBy { it.carId }.mapValues { (_, mediaList) -> mediaList }

        TopsUiState(
            categories = categories,
            selectedCategoryId = _selectedCategoryId.value,
            topCars = cars.map { cwd ->
                TopCarUiModel(
                    carId = cwd.car.id,
                    brandName = brandMap[cwd.car.brandId] ?: "",
                    category = categoryRepository.getCategoryById(cwd.car.categoryId).toString(),
                    year = cwd.car.year,
                    model = cwd.car.model,
                    powerHP = cwd.details?.powerHP ?: 0,
                    acceleration = cwd.details?.acceleration ?: 0.0,
                    maxSpeed = cwd.details?.maxSpeed ?: 0.0,
                    imageUrl = photosByCarId[cwd.car.id],
                    allMediaURLs = carMedia
                )
            },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TopsUiState())

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value =
            if (_selectedCategoryId.value == categoryId) null else categoryId
    }
}
