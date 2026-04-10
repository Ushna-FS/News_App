package com.example.newsapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.newsapp.R

@Composable
fun AppDrawer(
    onHomeClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onBookmarksClick: () -> Unit
) {

    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.7f)) {

        Spacer(modifier = Modifier.height(24.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.home)) },
            selected = false,
            icon = { Icon(Icons.Default.Home, null) },
            onClick = onHomeClick
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.discover)) },
            selected = false,
            icon = { Icon(Icons.Default.Search, null) },
            onClick = onDiscoverClick
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.saved)) },
            selected = false,
            icon = { Icon(Icons.Default.Bookmark, null) },
            onClick = onBookmarksClick
        )
    }
}