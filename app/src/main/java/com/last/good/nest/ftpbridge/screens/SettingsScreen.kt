package com.last.good.nest.ftpbridge.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.last.good.nest.ftpbridge.IPreferences
import com.last.good.nest.ftpbridge.component.PasswordField
import com.last.good.nest.ftpbridge.model.OutlinedGroup
import com.last.good.nest.ftpbridge.view.model.SettingsViewModel

@Composable
fun SettingsScreen(
    prefs: IPreferences? = null,
    goBack: () -> Unit,
    selectFolder: () -> Unit
) {
    val scrollState = rememberScrollState()
    val numberPattern = remember { Regex("^\\d*\$") }

    val viewModel = remember { SettingsViewModel(prefs) }

    val ftpPort by viewModel.ftpServerPort.collectAsStateWithLifecycle("2121")
    val deleteAfterSynced by viewModel.deleteAfterSynced.collectAsStateWithLifecycle(true)
    val useTempDirectoryState by viewModel.useTmpDir.collectAsStateWithLifecycle(true)
    val rootDirectoryState by viewModel.rootDir.collectAsStateWithLifecycle(null)
    val serverAddressState by viewModel.smbServerAddress.collectAsStateWithLifecycle(null)
    val ftpAllowAnonymous by viewModel.ftpAllowAnonymous.collectAsStateWithLifecycle(true)
    val ftpUsername by viewModel.ftpUsername.collectAsStateWithLifecycle("")
    val ftpPassword by viewModel.ftpPassword.collectAsStateWithLifecycle("")
    val smbPort by viewModel.smbServerPort.collectAsStateWithLifecycle(null)
    val shareName by viewModel.smbShareName.collectAsStateWithLifecycle("")
    val smbUsername by viewModel.smbUsername.collectAsStateWithLifecycle("")
    val smbPassword by viewModel.smbPassword.collectAsStateWithLifecycle("")
    val smbRemoteDir by viewModel.smbRemoteDir.collectAsStateWithLifecycle("")

    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Button(
                onClick = goBack,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Settings",
                )
            }
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            text = "Settings",
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = ftpPort,
                onValueChange = {
                    if (it.matches(numberPattern)) {
                        viewModel.setPortNumber(it)
                    }
                },
                label = { Text(text = "FTP Server Port") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedGroup(
                groupTitle = "File System",
                innerPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delete after file is synced",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = deleteAfterSynced,
                            onCheckedChange = { viewModel.setDeleteAfterSynced(it) },

                            )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use temporary directory",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = useTempDirectoryState,
                            onCheckedChange = { viewModel.setUseTmpDir(it) },
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = selectFolder, enabled = !useTempDirectoryState) {
                            Text(text = "Select directory")
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = rootDirectoryState.toString().ifBlank { "Not selected" },
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = if (useTempDirectoryState) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            OutlinedGroup(
                groupTitle = "FTP Authentication"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Allow anonymous user",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = ftpAllowAnonymous,
                            onCheckedChange = { viewModel.setFtpAllowAnonymous(it) },
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = ftpUsername,
                            onValueChange = {
                                viewModel.setFtpUsername(it)
                            },
                            label = { Text(text = "Username") },
                            singleLine = true
                        )
                        PasswordField(
                            modifier = Modifier.weight(1f),
                            value = ftpPassword,
                            onValueChange = {
                                viewModel.setFtpPassword(it)
                            },
                            label = { Text(text = "Password") }
                        )
                    }
                }            }
            OutlinedGroup(
                groupTitle = "Remote server"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        TextField(
                            modifier = Modifier.weight(2f),
                            value = serverAddressState ?: "",
                            onValueChange = {
                                viewModel.setSmbServerAddress(it)
                            },
                            label = { Text(text = "Server Address") },
                            singleLine = true
                        )
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = smbPort ?: "",
                            onValueChange = {
                                if (it.matches(numberPattern)) {
                                    viewModel.setSmbServerPort(it)
                                }
                            },
                            label = { Text(text = "Port") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = shareName,
                        onValueChange = {
                            viewModel.setSmbShareName(it)
                        },
                        label = { Text(text = "Share Name") },
                        singleLine = true
                    )
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = smbUsername,
                        onValueChange = {
                            viewModel.setSmbUsername(it)
                        },
                        label = { Text(text = "Username") },
                        singleLine = true
                    )
                    PasswordField(
                        modifier = Modifier.fillMaxWidth(),
                        value = smbPassword,
                        onValueChange = {
                            viewModel.setSmbPassword(it)
                        },
                        label = { Text(text = "Password") }
                    )
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = smbRemoteDir,
                        onValueChange = {
                            viewModel.setSmbRemoteDir(it)
                        },
                        label = { Text(text = "Sync Directory Destination") },
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        goBack = {},
        selectFolder = {}
    )
}
