package com.last.good.nest.ftpbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.last.good.nest.ftpbridge.navigation.Screen
import com.last.good.nest.ftpbridge.ui.theme.FtpBridgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FtpBridgeTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.Permission.route, Modifier.background(
                    MaterialTheme.colorScheme.background)) {
                    composable(route = Screen.Permission.route) {
                        PermissionScreen(
                            onPermissionGranted = {
                                navController.popBackStack()
                                navController.navigate(Screen.Main.route)
                            }
                        )
                    }
                    composable(route = Screen.Main.route) {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FtpBridgeTheme {
        Greeting("Android")
    }
}