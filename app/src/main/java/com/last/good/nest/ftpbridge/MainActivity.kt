package com.last.good.nest.ftpbridge

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.last.good.nest.ftpbridge.navigation.Screen
import com.last.good.nest.ftpbridge.screens.PermissionScreen
import com.last.good.nest.ftpbridge.screens.SettingsScreen
import com.last.good.nest.ftpbridge.ui.theme.FtpBridgeTheme
import com.last.good.nest.ftpbridge.utils.FileUtils.toFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = IPreferences.of(applicationContext)
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        val folderPickerLauncher = activityResultLauncher(coroutineScope, preferences)

        setContent {
            FtpBridgeTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Permission.route,
                    Modifier.background(
                        MaterialTheme.colorScheme.background
                    ),
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
                ) {
                    composable(route = Screen.Permission.route) {
                        PermissionScreen(
                            onPermissionGranted = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Permission.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                    composable(route = Screen.Main.route) {
                        App(navigation = navController, prefs = remember { preferences })
                    }
                    composable(route = Screen.Settings.route) {
                        SettingsScreen(
                            prefs = remember { preferences },
                            goBack = { navController.popBackStack() },
                            selectFolder = {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                                folderPickerLauncher.launch(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun ComponentActivity.activityResultLauncher(
    coroutineScope: CoroutineScope,
    preferences: DataStorePreferences
) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == RESULT_OK) {
        val uri = result.data?.data ?: return@registerForActivityResult

        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        coroutineScope.launch {
            val path = uri.path?.split(":")?.get(1)
            val dir = if (path == null) {
                null
            } else {
                "/storage/emulated/0/$path".toFile()
            }

            preferences.setRootDirectory(dir)
        }
    }
}
