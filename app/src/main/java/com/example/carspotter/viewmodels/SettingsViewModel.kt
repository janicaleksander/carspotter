package com.example.carspotter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.BuildConfig
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.Settings
import com.example.carspotter.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings.asStateFlow()

    private val _random_photo = MutableStateFlow<String?>(null)
    val random_photo: StateFlow<String?> = _random_photo.asStateFlow()
    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _settings.value = settingsRepository.getSettings()
            _random_photo.value = settingsRepository.getRandomPhoto(BuildConfig.BUCKET_ID)
        }
    }



}
