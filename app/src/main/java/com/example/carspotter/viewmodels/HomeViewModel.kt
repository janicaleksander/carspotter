package com.example.carspotter.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.models.User
import com.example.carspotter.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val carRepository: CarRepository
): ViewModel() {
    var currentUserId: String? = "";
    init {
        viewModelScope.launch {
            currentUserId = accountService.getLoggedIn()?.id;
        }
    }
    val x = carRepository.getTopCars();

    init{
        //todo

    }
}