package com.last.good.nest.ftpbridge.utils

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    fun getLocalIpAddress(): Inet4Address? {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isUp && !networkInterface.isLoopback) {
                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    if (inetAddress.hostAddress.startsWith("10")) {
                        continue
                    }
                    if (inetAddress is Inet4Address) {
                        return inetAddress
                    }
                }
            }
        }

        return null
    }
}