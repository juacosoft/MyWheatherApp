@file:OptIn(ExperimentalMaterial3Api::class)

package com.mtzdev.mywheatherapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mtzdev.mywheatherapp.ui.theme.MyWheatherAppTheme

@Composable
fun TopBarComponent(
    title: String,
    loading: Boolean = false,
    onLeftIconClick: () -> Unit,
    onRightIconClick: (RightIconClicked) -> Unit
){
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        TopAppBar(
            title = {
                if (loading) {
                    Text("...")
                }else {
                    Text(title)
                }
            },
            navigationIcon = {
                TopBarIcon(
                    enable = !loading,
                    imageVector = Icons.Default.LocationOn,
                    onClick = onLeftIconClick
                )
            },
            actions = {
                TopBarIcon(
                    imageVector = Icons.Default.Notifications,
                    onClick = { onRightIconClick(RightIconClicked.NOTIFICATION) })
                Spacer(modifier = Modifier.width(4.dp))
                TopBarIcon(
                    imageVector = Icons.Default.Settings,
                    onClick = { onRightIconClick(RightIconClicked.SETTINGS) }
                )
            }

        )
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

enum class RightIconClicked {
    NOTIFICATION, SETTINGS
}

@Composable
private fun TopBarIcon(
    imageVector: ImageVector,
    enable: Boolean = true,
    onClick: () -> Unit) {
    IconButton(
        enabled = enable,
        onClick = onClick,
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
            shape = MaterialTheme.shapes.large
        )
    ) {
        Icon(imageVector, contentDescription = null)
    }
}

@Preview(showBackground = false, showSystemUi = true
)
@Composable
fun TopBarComponent_Preview() {
    MyWheatherAppTheme {
        TopBarComponent(title = "London", loading = true, {}, {})
    }
}