package com.last.good.nest.ftpbridge.navigation

sealed class Screen(val route: String) {
    data object Permission : Screen(route = "permission_screen")
    data object Main : Screen(route = "main_screen")
    data object Settings : Screen(route = "general_settings")
}