package com.last.good.nest.ftpbridge.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.last.good.nest.ftpbridge.IPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import java.io.File

@Composable
fun SettingsScreen(
    prefs: IPreferences? = null,
    goBack: () -> Unit,
    selectFolder: () -> Unit
) {
    val numberPattern = remember { Regex("^\\d*\$") }

    var portNumberState by remember { mutableStateOf(TextFieldValue()) }
    var deleteAfterSynced by remember { mutableStateOf(true) }
    var useTempDirectoryState by remember { mutableStateOf(true) }
    var rootDirectoryState: File? by remember { mutableStateOf(null) }

    val portFlow by (prefs?.port ?: flowOf(2121)).collectAsStateWithLifecycle(2121)
    val deleteAfterSyncedFlow by (prefs?.deleteAfterSynced ?: flowOf(true)).collectAsStateWithLifecycle(true)
    val useTempDirectoryFlow by (prefs?.useTmpDir ?: flowOf(true)).collectAsStateWithLifecycle(true)
    val rootDirectoryFlow by (prefs?.rootDirectory ?: flowOf(null)).collectAsStateWithLifecycle(null)

    LaunchedEffect(Unit) {
        if (prefs == null) {
            return@LaunchedEffect
        }

        portNumberState = TextFieldValue(prefs.port.first().toString())
        deleteAfterSynced = prefs.deleteAfterSynced.first()
        useTempDirectoryState = prefs.useTmpDir.first()
        rootDirectoryState = prefs.rootDirectory.first()
    }

    LaunchedEffect(portFlow) {
        portNumberState = TextFieldValue(portFlow.toString())
    }

    LaunchedEffect(deleteAfterSyncedFlow) {
        deleteAfterSynced = deleteAfterSyncedFlow
    }

    LaunchedEffect(useTempDirectoryFlow) {
        useTempDirectoryState = useTempDirectoryFlow
    }

    LaunchedEffect(rootDirectoryFlow) {
        rootDirectoryState = rootDirectoryFlow
    }

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
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = portNumberState,
                onValueChange = {
                    if (it.text.matches(numberPattern)) {
                        portNumberState = it
                    }
                },
                label = { Text(text = "FTP Server Port") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Box(
                modifier = Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(5.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
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
                            onCheckedChange = { deleteAfterSynced = it },

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
                            onCheckedChange = { useTempDirectoryState = it },

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
