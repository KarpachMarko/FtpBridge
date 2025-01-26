package com.last.good.nest.ftpbridge.utils

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {

    fun getLocalIPv4Address(): InetAddress? {
        val hotspotAddress = getIPv4FromInterfaces("swlan")
        val wifiAddress = getIPv4FromInterfaces("wlan")
        return hotspotAddress ?: wifiAddress
    }

    private fun getIPv4FromInterfaces(interfacePrefix: String): InetAddress? {
        return NetworkInterface.getNetworkInterfaces().asSequence()
            .filter { it.name.startsWith(interfacePrefix, ignoreCase = true) }
            .flatMap { it.inetAddresses.asSequence() }
            .find { it is Inet4Address && !it.isLoopbackAddress }
    }
}