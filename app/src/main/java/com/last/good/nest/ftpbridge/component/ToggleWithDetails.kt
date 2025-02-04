package com.last.good.nest.ftpbridge.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ToggleWithDetails(
    toggleLabel: String,
    enabled: Boolean = true,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    mainRowBg: Color = MaterialTheme.colorScheme.primaryContainer,
    mainRowColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    detailRowBg: Color = MaterialTheme.colorScheme.secondaryContainer,
    detailRowColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    detailsContent: (@Composable RowScope.() -> Unit)? = null,
    detailsRows: List<List<String>> = emptyList()
) {

    val hasDetails = detailsRows.isNotEmpty() || detailsContent != null
    val mainRowBottomRadius = if (hasDetails) 0 else 10

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = mainRowBg,
                    shape = RoundedCornerShape(
                        topStart = 10.dp, topEnd = 10.dp,
                        bottomStart = mainRowBottomRadius.dp, bottomEnd = mainRowBottomRadius.dp
                    )
                )
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = toggleLabel,
                color = mainRowColor,
            )
            Switch(
                enabled = enabled,
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = detailRowBg,
                    shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                )
        ) {

            Column DetailsColumn@{

                if (detailsContent != null) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                            .fillMaxWidth(),
                        content = detailsContent
                    )
                }

                detailsRows.forEach { details ->
                    Row(
                        modifier = Modifier
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = if (details.size > 1) Arrangement.SpaceBetween else Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        details.forEach {
                            Text(
                                text = it,
                                color = detailRowColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewFtpService() {
    ToggleWithDetails(
        toggleLabel = "FTP service",
        enabled = true,
        checked = false,
        onCheckedChange = {},
        detailsRows = listOf(
            listOf("Warning message"),
            listOf("IP", "192.168.1.1"),
            listOf("Port", "21")
        )
    )
}