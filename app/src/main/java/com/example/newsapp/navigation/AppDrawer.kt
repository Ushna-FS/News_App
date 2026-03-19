package com.example.newsapp.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    onHomeClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onBookmarksClick: () -> Unit
) {

    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.7f)) {

        Spacer(modifier = Modifier.height(24.dp))

        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            icon = { Icon(Icons.Default.Home, null) },
            onClick = onHomeClick
        )

        NavigationDrawerItem(
            label = { Text("Discover") },
            selected = false,
            icon = { Icon(Icons.Default.Search, null) },
            onClick = onDiscoverClick
        )

        NavigationDrawerItem(
            label = { Text("Saved") },
            selected = false,
            icon = { Icon(Icons.Default.Bookmark, null) },
            onClick = onBookmarksClick
        )
    }
}