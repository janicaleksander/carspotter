package com.example.carspotter.dao

import androidx.room.Query

interface HealtCheckDao {
    @Query("SELECT 1")
    fun healthCheck()
}