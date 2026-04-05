package com.example.newsapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.newsapp.R

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
                Icon(Icons.Default.Menu, contentDescription = null)
            }
        }
    )
}

@Preview(showBackground = true, name = "Main Toolbar Preview")
@Composable
fun MainToolbarPreview() {
    MaterialTheme {
        MainToolbar(
            title = stringResource(R.string.newsmate),
            onMenuClick = {}
        )
    }
}