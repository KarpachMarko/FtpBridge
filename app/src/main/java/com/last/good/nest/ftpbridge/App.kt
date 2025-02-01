package com.last.good.nest.ftpbridge

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.last.good.nest.ftpbridge.component.SettingsRow
import com.last.good.nest.ftpbridge.component.ToggleWithDetails
import com.last.good.nest.ftpbridge.model.NetworkViewModel
import com.last.good.nest.ftpbridge.services.BridgeFtpServer
import com.last.good.nest.ftpbridge.services.SyncService


@Composable
fun App(
    viewModel: NetworkViewModel = viewModel(),
    navigation: NavHostController,
    prefs: IPreferences
) {
    val context = LocalContext.current

    val ftpAddress by viewModel.ipAddress.collectAsStateWithLifecycle()
    val ftpServerPort by prefs.port.collectAsStateWithLifecycle(0)
    var loaded by remember { mutableStateOf(false) }

    var ftpServiceIntent: Intent by remember { mutableStateOf(BridgeFtpServer.getDefaultIntent(context)) }
    var syncServiceIntent: Intent by remember { mutableStateOf(SyncService.getDefaultIntent(context)) }

    fun isFtpRunning() = BridgeFtpServer.getState().isRunning()
    fun isSyncRunning() = SyncService.getState().isRunning()

    fun toggleFtpServer(start: Boolean) {
        if (start) {
            context.startService(ftpServiceIntent)
        } else {
            context.stopService(ftpServiceIntent)
        }
    }

    fun toggleSyncServer(start: Boolean) {
        if (start) {
            context.startService(syncServiceIntent)
        } else {
            context.stopService(syncServiceIntent)
        }
    }

    LaunchedEffect(Unit) {
        ftpServiceIntent = BridgeFtpServer.getIntentFromPrefs(context, prefs)
        syncServiceIntent = SyncService.getIntentFromPrefs(context, prefs)
        loaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.2f))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            text = "FTP Bridge",
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.primary
        )
        SettingsRow(navigation)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToggleWithDetails(
                toggleLabel = "FTP service",
                enabled = loaded && ftpAddress != null,
                checked = isFtpRunning(),
                onCheckedChange = ::toggleFtpServer,
                detailsRows = if (ftpAddress == null) listOf(listOf("Turn on HotSpot or connect to Wi-Fi"))
                else listOf(
                    listOf("IP", ftpAddress!!.hostAddress),
                    listOf("Port", ftpServerPort.toString())
                )
            )
            ToggleWithDetails(
                toggleLabel = "Sync service",
                enabled = loaded,
                checked = isSyncRunning(),
                onCheckedChange = ::toggleSyncServer
            )
        }
    }
}
