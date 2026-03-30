package com.example.carspotter.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.carspotter.models.User
import com.example.carspotter.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val carRepository: CarRepository
): ViewModel() {
    val currentUserId: String = checkNotNull(savedStateHandle["userId"]){
        //todo
    }
    val x = carRepository.getTopCars();

    init{
        //todo

    }
}