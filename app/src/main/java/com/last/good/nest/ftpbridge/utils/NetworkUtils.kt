package com.last.good.nest.ftpbridge.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {

    fun getLocalIPv4Address(context: Context): InetAddress? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return null
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return null

        return when {
            // Case 1: Phone is streaming its own hotspot
            hotSpotEnabled(networkCapabilities) -> getIPv4FromInterfaces(
                "swlan"
            )

            // Case 2: Connected to Wi-Fi
            wifiEnabled(networkCapabilities) -> getIPv4FromInterfaces(
                "wlan"
            )

            else -> null
        }
    }

    private fun wifiEnabled(networkCapabilities: NetworkCapabilities) =
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

    private fun hotSpotEnabled(networkCapabilities: NetworkCapabilities) =
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

    private fun getIPv4FromInterfaces(interfacePrefix: String): InetAddress? {
        return NetworkInterface.getNetworkInterfaces().asSequence()
            .filter { it.name.startsWith(interfacePrefix, ignoreCase = true) }
            .flatMap { it.inetAddresses.asSequence() }
            .find { it is Inet4Address && !it.isLoopbackAddress }
    }
}