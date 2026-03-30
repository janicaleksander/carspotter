package com.example.carspotter.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.carspotter.models.User
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    val currentUserId: String = checkNotNull(savedStateHandle["userId"]){
        //todo
    }

    init{
        //todo

    }
}