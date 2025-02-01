package com.last.good.nest.ftpbridge.model

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.last.good.nest.ftpbridge.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.net.InetAddress

class NetworkViewModel(application: Application) : AndroidViewModel(application) {

    private val _ipAddress: MutableStateFlow<InetAddress?> = MutableStateFlow(null)
    val ipAddress = _ipAddress
        .onStart { observeNetworkChanges() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val connectivityManager = application.getSystemService(ConnectivityManager::class.java)

    private fun observeNetworkChanges() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateIpAddress()
            }

            override fun onLost(network: Network) {
                updateIpAddress()
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun updateIpAddress() {
        val newIp = NetworkUtils.getLocalIPv4Address()
        _ipAddress.update { newIp }
    }
}