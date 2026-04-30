package com.example.carspotter

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.example.carspotter.auth.AccountService
import com.example.carspotter.database.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import io.appwrite.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider, ImageLoaderFactory {

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
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // Ustawienie pamięci podręcznej RAM (opcjonalne, ale zalecane)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            // KLUCZOWE: Konfiguracja pamięci dyskowej
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    // Zwiększamy cache do 250 MB. Pliki 8K ważą dużo,
                    // mały cache spowoduje ciągłe nadpisywanie i pobieranie od nowa.
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            // Włącz logger na etapie dewelopmentu, aby widzieć w Logcat,
            // czy pliki uderzają w "DISK" czy w "NETWORK".
            .logger(DebugLogger())
            .build()
    }
}
