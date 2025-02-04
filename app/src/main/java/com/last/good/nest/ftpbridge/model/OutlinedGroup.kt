package com.last.good.nest.ftpbridge.model

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun OutlinedGroup(
    groupTitle: String,
    modifier: Modifier = Modifier,
    innerPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(
        10.dp
    ),
    content: @Composable() (androidx.compose.foundation.layout.BoxScope.() -> Unit) = {}
) {
    val density = LocalDensity.current

    var groupLabelHeight by remember { mutableStateOf(0.dp) }
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(top = groupLabelHeight / 2)
    ) {
        Text(
            text = groupTitle,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .onGloballyPositioned { groupLabelHeight = (it.size.height / density.density).dp }
                .offset {
                    IntOffset(
                        x = with(this) { 15.dp.roundToPx() },
                        y = with(this) { -(groupLabelHeight / 2).roundToPx() })
                }
                .zIndex(1f)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 10.dp)
        )
        Box(
            modifier = Modifier
                .wrapContentSize()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(5.dp)
                )
                .padding(top = groupLabelHeight / 2)
                .padding(innerPadding),
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OutlinedGroupPreview() {
    OutlinedGroup(
        groupTitle = "Group title",
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(300.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TextField("input 1", { }, Modifier.fillMaxWidth())
            TextField("input 2", { }, Modifier.fillMaxWidth())
            TextField("input 3", { }, Modifier.fillMaxWidth())
        }
    }
}