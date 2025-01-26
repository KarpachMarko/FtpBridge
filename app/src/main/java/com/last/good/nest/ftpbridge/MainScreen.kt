package com.last.good.nest.ftpbridge

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.last.good.nest.ftpbridge.model.NetworkViewModel
import com.last.good.nest.ftpbridge.services.BridgeFtpServer
import com.last.good.nest.ftpbridge.services.SyncService


@Preview(showBackground = true)
@Composable
fun MainScreen(viewModel: NetworkViewModel = viewModel()) {
    val context = LocalContext.current
    val ftpServiceIntent = Intent(context, BridgeFtpServer::class.java)
    val syncServiceIntent = Intent(context, SyncService::class.java)

    val ftpAddress by viewModel.ipAddress.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxHeight(0.2f))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            text = "FTP Bridge",
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FTP server",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Switch(
                            enabled = ftpAddress != null,
                            checked = isFtpRunning(),
                            onCheckedChange = ::toggleFtpServer,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                        )
                ) {
                    Column DetailsColumn@{
                        if (ftpAddress == null) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 5.dp, horizontal = 10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Turn on HotSpot or connect to Wi-Fi",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = TextAlign.Center
                                )

                            }
                            return@DetailsColumn
                        }

                        Row(
                            modifier = Modifier
                                .padding(vertical = 5.dp, horizontal = 10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "IP",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = ftpAddress?.hostAddress ?: "N/A",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Row(
                            modifier = Modifier
                                .padding(vertical = 5.dp, horizontal = 10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Port",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "2121", //FIXME get value from config
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sync server",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Switch(
                        checked = isSyncRunning(),
                        onCheckedChange = ::toggleSyncServer,
                    )
                }
            }
        }
    }
}