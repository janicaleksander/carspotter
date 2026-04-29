package com.example.carspotter.network_monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

interface INetworkMonitor {
    val isOnline: Flow<Boolean>
}
class NetworkMonitor @Inject constructor(
    @ApplicationContext context: Context
) : INetworkMonitor{
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    override val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback(){
            override fun onAvailable(network: android.net.Network) {
                trySend(true)
            }
            override fun onLost(network: android.net.Network) {
                trySend(false)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)


        trySend(connectivityManager.activeNetwork?.let { connectivityManager.getNetworkCapabilities(it)?.hasCapability(NET_CAPABILITY_INTERNET) } ?: false)
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}