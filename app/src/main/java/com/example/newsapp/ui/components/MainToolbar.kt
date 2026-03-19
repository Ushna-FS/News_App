package com.example.newsapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainToolbar(
    title: String,
    onMenuClick: () -> Unit
) {

    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}

@Preview(showBackground = true, name = "Main Toolbar Preview")
@Composable
fun MainToolbarPreview() {
    MaterialTheme {
        MainToolbar(
            title = "News App",
            onMenuClick = {}
        )
    }
}