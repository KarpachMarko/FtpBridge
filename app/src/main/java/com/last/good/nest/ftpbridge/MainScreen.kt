package com.last.good.nest.ftpbridge

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.last.good.nest.ftpbridge.services.BridgeFtpServer
import com.last.good.nest.ftpbridge.services.SyncService


@Preview(showBackground = true)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val ftpServiceIntent = Intent(context, BridgeFtpServer::class.java)
    val syncServiceIntent = Intent(context, SyncService::class.java)

    fun isFtpRunning() = BridgeFtpServer.getState().isRunning()
    fun isSyncRunning() = SyncService.getState().isRunning()
    fun isBridgeRunning() = isFtpRunning() && isSyncRunning()

    fun toggleService() {
        if (!isBridgeRunning()) {
            context.startService(ftpServiceIntent)
            context.startService(syncServiceIntent)
        } else {
            context.stopService(ftpServiceIntent)
            context.stopService(syncServiceIntent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            text = "FTP Bridge",
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Text(
                text = "FTP service: ${BridgeFtpServer.getState()}",
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Sync service: ${SyncService.getState()}",
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Button(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .padding(20.dp)
                .aspectRatio(1f),
            onClick = { toggleService() },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(text = if (isBridgeRunning()) "Syncing..." else "Sync", fontSize = 30.sp)
        }
    }
}