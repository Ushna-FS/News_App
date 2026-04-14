package com.example.shared.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.viewmodels.AuthViewModel
import me.sample.library.resources.Res
import me.sample.library.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppDrawer(
    onHomeClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onLogoutClick: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()

) {

    var showLogoutDialog by remember { mutableStateOf(false) }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(Res.string.logout)) },
            text = { Text(stringResource(Res.string.logout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.7f)) {
        AppDrawerHeader(authViewModel)

        Spacer(modifier = Modifier.height(24.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.home)) },
            selected = false,
            icon = { Icon(Icons.Default.Home, null) },
            onClick = onHomeClick
        )

        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.discover)) },
            selected = false,
            icon = { Icon(Icons.Default.Search, null) },
            onClick = onDiscoverClick
        )

        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.saved)) },
            selected = false,
            icon = { Icon(Icons.Default.Bookmark, null) },
            onClick = onBookmarksClick
        )

        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.logout)) },
            selected = false,
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
            onClick = { showLogoutDialog = true }
        )
    }
}


@Composable
fun AppDrawerHeader(authViewModel: AuthViewModel) {
    var username by remember { mutableStateOf<String?>(null) }
    val user = authViewModel.currentUser()

    val backgroundColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    LaunchedEffect(user?.uid) {
        if (user != null) {
            authViewModel.getCurrentUsername { fetchedName ->
                username = fetchedName
            }
        }
    }
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
                contentDescription = stringResource(Res.string.profile),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Username
            Text(
                text = username ?: stringResource(Res.string.username),
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Email
            Text(
                text = user?.email ?: stringResource(Res.string.mail),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}