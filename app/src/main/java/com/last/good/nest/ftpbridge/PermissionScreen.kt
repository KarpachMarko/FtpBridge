package com.last.good.nest.ftpbridge

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@Composable
fun PermissionScreen(onPermissionGranted: () -> Unit) {
    RequestPermissions(onPermissionGranted = onPermissionGranted)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestPermissions(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current

    val notificationPermissionState: PermissionState? =
        if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            null
        }

    val areNotificationsAllowed = { notificationPermissionState?.status?.isGranted != false }
    val isStoragePermissionGranted = { Environment.isExternalStorageManager() }

    fun checkPermissions() {
        if (areNotificationsAllowed() && isStoragePermissionGranted()) {
            return onPermissionGranted()
        }
    }

    fun openNotificationSettings() {

        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
        checkPermissions()
    }

    fun launchStoragePermissionIntent() {
        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(this)
        }
        checkPermissions()
    }

    checkPermissions()

    return Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 20.dp),
            text = "Permissions are important for this app. Please grant the permissions.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (!areNotificationsAllowed()) {
            Button(onClick = { openNotificationSettings() }) {
                Text("Request Notification permission")
                notificationPermissionState?.launchPermissionRequest()
            }
        }

        if (!isStoragePermissionGranted()) {
            Button(onClick = { launchStoragePermissionIntent() }) {
                Text("Request Storage permission")
            }
        }
    }
}