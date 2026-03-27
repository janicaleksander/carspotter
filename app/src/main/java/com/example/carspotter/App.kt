package com.example.carspotter

import android.app.Application
import com.example.carspotter.database.AppDatabase
import com.example.carspotter.database.AppwriteModule
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var localDatabase: AppDatabase

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            localDatabase.openHelper.writableDatabase
        }
    }
}