package com.example.carspotter.repository

data class SyncedCarResult(
    val allCarIds: List<String>,
    val topCarIds: List<String>
)
