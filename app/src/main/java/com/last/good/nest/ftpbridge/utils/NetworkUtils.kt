package com.last.good.nest.ftpbridge.utils

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {

    fun getLocalIPv4Address(): InetAddress? {
        return getIPv4FromInterfaces("swlan") ?: getIPv4FromInterfaces("wlan")
    }

    private fun getIPv4FromInterfaces(interfacePrefix: String): InetAddress? {
        return NetworkInterface.getNetworkInterfaces().asSequence()
            .filter { it.name.startsWith(interfacePrefix, ignoreCase = true) }
            .flatMap { it.inetAddresses.asSequence() }
            .find { it is Inet4Address && !it.isLoopbackAddress }
    }
}