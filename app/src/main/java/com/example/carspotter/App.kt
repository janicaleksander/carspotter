package com.example.carspotter

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.carspotter.auth.AccountService
import com.example.carspotter.database.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import io.appwrite.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var localDatabase: AppDatabase
    @Inject lateinit var workerFactory: HiltWorkerFactory


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            localDatabase.openHelper.writableDatabase
        }
    }
}

//TODO w widku szczegolow top dtail jest taki bialy pasek jak sie zjedza w dol jakby bial ypasek na dolnyn bottom bar
//potwierdzeni czy napewno chcezz usunac swoje auto i po usnecou teleport do poprzednieog widoku
//i ma nie niebyc go tab