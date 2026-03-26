package com.example.newsapp.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.R
import com.example.newsapp.viewmodels.AuthViewModel

@Composable
fun AppDrawer(
    onHomeClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onLogoutClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()

) {

    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.7f)) {
        AppDrawerHeader(authViewModel)

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

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.logout)) },
            selected = false,
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
            onClick = onLogoutClick
        )
    }
}



@Composable
fun AppDrawerHeader(authViewModel: AuthViewModel) {
    val user = authViewModel.currentUser()

    val backgroundColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Profile Image
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = stringResource(R.string.profile),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Username
            Text(
                text = user?.displayName ?: stringResource(R.string.username),
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )

            // Email
            Text(
                text = user?.email ?: stringResource(R.string.mail),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}