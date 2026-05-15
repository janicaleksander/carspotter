package com.example.carspotter.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Brand
import com.example.carspotter.models.Car
import com.example.carspotter.models.Category
import com.example.carspotter.models.Location
import com.example.carspotter.models.Media
import com.example.carspotter.models.MediaTypeEnum
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject

data class PickedMedia(
    val localPath: String,
    val type: MediaTypeEnum,
)

sealed interface SaveSpotState {
    data object Idle : SaveSpotState
    data object Saving : SaveSpotState
    data object Success : SaveSpotState
    data class Error(val message: String) : SaveSpotState
}

private data class NewSpotForm(
    val brandId: String? = null,
    val categoryId: String? = null,
    val model: String = "",
    val year: String = "",
    val price: String = "",
    val notes: String = "",
    val location: Pair<Double, Double>? = null,
    val media: List<PickedMedia> = emptyList(),
)

data class NewSpotErrors(
    val media: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val model: String? = null,
    val year: String? = null,
    val price: String? = null,
    val location: String? = null,
    val notes: String? = null,
) {
    val hasAny: Boolean get() =
        listOf(media, brand, category, model, year, price, location, notes).any { it != null }
}

data class NewSpotUiState(
    val brands: List<Brand> = emptyList(),
    val categories: List<Category> = emptyList(),
    val brandId: String? = null,
    val categoryId: String? = null,
    val model: String = "",
    val year: String = "",
    val price: String = "",
    val notes: String = "",
    val location: Pair<Double, Double>? = null,
    val locationLabel: String? = null,
    val media: List<PickedMedia> = emptyList(),
    val isOnline: Boolean = false,
    val saveState: SaveSpotState = SaveSpotState.Idle,
    val errors: NewSpotErrors = NewSpotErrors(),
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
    private val _form = MutableStateFlow(NewSpotForm())
    private val _saveState = MutableStateFlow<SaveSpotState>(SaveSpotState.Idle)
    private val _showErrors = MutableStateFlow(false)
    private val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            userId.value = withContext(Dispatchers.IO) {
                accountService.getLoggedIn()?.id
            }
        }
    }

    val uiState: StateFlow<NewSpotUiState> = combine(
        combine(_form, _showErrors) { f, s -> f to s },
        brandRepository.getBrands(),
        categoryRepository.getCategories(),
        isOnline,
        _saveState,
    ) { (form, showErrors), brands, categories, online, save ->
        val errors = validate(form)
        NewSpotUiState(
            brands = brands,
            categories = categories,
            brandId = form.brandId,
            categoryId = form.categoryId,
            model = form.model,
            year = form.year,
            price = form.price,
            notes = form.notes,
            location = form.location,
            locationLabel = form.location?.let { (lat, lon) ->
                String.format(Locale.US, "%.5f, %.5f", lat, lon)
            },
            media = form.media,
            isOnline = online,
            saveState = save,
            errors = if (showErrors) errors else NewSpotErrors(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewSpotUiState())


    fun onBrandSelected(id: String?) = update { it.copy(brandId = id) }
    fun onCategorySelected(id: String?) = update { it.copy(categoryId = id) }
    fun onModelChange(value: String) = update { it.copy(model = value) }
    fun onYearChange(value: String) = update { it.copy(year = value.filter(Char::isDigit).take(4)) }
    fun onPriceChange(value: String) = update { it.copy(price = sanitizePrice(value)) }
    fun onNotesChange(value: String) = update { it.copy(notes = value) }
    fun onLocationSelected(lat: Double, lon: Double) = update { it.copy(location = lat to lon) }

    fun addPhoto(localPath: String) = update {
        it.copy(media = it.media + PickedMedia(localPath, MediaTypeEnum.PHOTO))
    }

    fun addVideo(localPath: String) = update {
        it.copy(media = it.media + PickedMedia(localPath, MediaTypeEnum.VIDEO))
    }

    /** Replaces any previously picked audio — only one engine sound per spot. */
    fun setAudio(localPath: String) = update { state ->
        val withoutAudio = state.media.filter { it.type != MediaTypeEnum.AUDIO }
        state.copy(media = withoutAudio + PickedMedia(localPath, MediaTypeEnum.AUDIO))
    }

    fun removeMedia(localPath: String) = update { state ->
        state.copy(media = state.media.filter { it.localPath != localPath })
    }

    private fun update(block: (NewSpotForm) -> NewSpotForm) {
        _form.value = block(_form.value)
    }

    private fun sanitizePrice(value: String): String {
        val raw = value.replace(',', '.').filter { it.isDigit() || it == '.' }
        val firstDot = raw.indexOf('.')
        return if (firstDot < 0) raw
        else raw.substring(0, firstDot + 1) + raw.substring(firstDot + 1).replace(".", "")
    }


    private fun validate(form: NewSpotForm): NewSpotErrors {
        val mediaErr = when {
            form.media.none { it.type == MediaTypeEnum.PHOTO } -> "Add at least one photo"
            form.media.count { it.type == MediaTypeEnum.AUDIO } > 1 -> "Only one engine sound is allowed"
            else -> null
        }
        val yearInt = form.year.toIntOrNull()
        val yearErr = when {
            form.year.isBlank() -> "Year is required"
            yearInt == null || yearInt !in 1900..(LocalDateTime.now().year + 1) -> "Enter a valid year"
            else -> null
        }
        val priceVal = form.price.toDoubleOrNull()
        val priceErr = when {
            form.price.isBlank() -> "Price is required"
            priceVal == null || priceVal < 0.0 -> "Enter a valid price"
            else -> null
        }
        val notesErr = if (form.notes.isBlank()) "Notes are required" else null
        return NewSpotErrors(
            media = mediaErr,
            brand = if (form.brandId.isNullOrBlank()) "Pick a brand" else null,
            category = if (form.categoryId.isNullOrBlank()) "Pick a category" else null,
            model = if (form.model.trim().isEmpty()) "Model is required" else null,
            year = yearErr,
            price = priceErr,
            location = if (form.location == null) "Pick a location on the map" else null,
            notes = notesErr,
        )
    }


    fun saveSpot() {
        if (_saveState.value is SaveSpotState.Saving) return
        // Reveal inline field errors from now on, even if the user hasn't typed
        // in a single field yet.
        _showErrors.value = true

        val form = _form.value
        val uid = userId.value
        val errors = validate(form)
        val blocker = when {
            uid == null -> "Not signed in"
            !isOnline.value -> "Offline — connect to the internet to save"
            errors.hasAny -> "Fix the highlighted fields"
            else -> null
        }
        if (blocker != null) {
            _saveState.value = SaveSpotState.Error(blocker)
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveSpotState.Saving
            try {
                val carId = ID.unique()
                val brandId = checkNotNull(form.brandId) { "brandId null after validation" }
                val categoryId = checkNotNull(form.categoryId) { "categoryId null after validation" }
                val currentUid = checkNotNull(uid) { "userId null after validation" }
                val loc = checkNotNull(form.location) { "location null after validation" }

                carRepository.insertCar(
                    Car(
                        id = carId,
                        brandId = brandId,
                        categoryId = categoryId,
                        model = form.model.trim(),
                        year = form.year.toInt(),
                        price = form.price.toDouble(),
                        isTop = false,
                        updatedAt = LocalDateTime.now(),
                        syncState = SyncState.PENDING_CREATE,
                    )
                )
                carRepository.pushPending()

                userCarRepository.insertUserCar(
                    UserCar(
                        id = ID.unique(),
                        userId = currentUid,
                        carId = carId,
                        notes = form.notes.trim(),
                        location = Location(
                            latitude = loc.first,
                            longitude = loc.second,
                        ),
                        updatedAt = LocalDateTime.now(),
                        syncState = SyncState.PENDING_CREATE,
                    )
                )
                userCarRepository.pushPending()

                val mediaToUpload = form.media.map { picked ->
                    Media(
                        carId = carId,
                        type = picked.type,
                        filePath = picked.localPath,
                        updatedAt = LocalDateTime.now(),
                    )
                }
                mediaRepository.uploadAndSaveMedia(carId, mediaToUpload)

                _saveState.value = SaveSpotState.Success
            } catch (e: Exception) {
                Log.e("NewSpotViewModel", "Failed to save spot", e)
                _saveState.value = SaveSpotState.Error(e.message ?: "Failed to save spot")
            }
        }
    }

    fun consumeSaveResult() {
        _saveState.value = SaveSpotState.Idle
    }
}
