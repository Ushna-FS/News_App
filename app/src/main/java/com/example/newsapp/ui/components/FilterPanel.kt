package com.example.newsapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newsapp.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shared.utils.ArticleCategoryMapper
import com.example.newsapp.viewmodels.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterPanel(
    viewModel: NewsViewModel,
    onApply: @Composable () -> Unit,
    onClose: () -> Unit
) {
    // Collect states with lifecycle awareness
    val availableSources by viewModel.availableSources.collectAsStateWithLifecycle()
    val selectedSources by viewModel.selectedSources.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()

    // Local state for UI
    var localSelectedCategories by remember { mutableStateOf(selectedCategories.toSet()) }
    var localSelectedSources by remember { mutableStateOf(selectedSources.toSet()) }

    // Update local state when ViewModel state changes
    LaunchedEffect(selectedCategories, selectedSources) {
        localSelectedCategories = selectedCategories.toSet()
        localSelectedSources = selectedSources.toSet()
    }

    val categories = ArticleCategoryMapper.categories.filter { it != stringResource(R.string.all) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.filter_news),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Categories Section
        Text(
            text = stringResource(R.string.categories),
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = localSelectedCategories.contains(category),
                    onClick = {
                        localSelectedCategories = if (localSelectedCategories.contains(category)) {
                            localSelectedCategories - category
                        } else {
                            localSelectedCategories + category
                        }
                    },
                    label = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sources Section
        if (availableSources.isNotEmpty()) {
            Text(
                text = stringResource(R.string.sources),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableSources.toList()) { source ->
                    FilterChip(
                        selected = localSelectedSources.contains(source),
                        onClick = {
                            localSelectedSources = if (localSelectedSources.contains(source)) {
                                localSelectedSources - source
                            } else {
                                localSelectedSources + source
                            }
                        },
                        label = {
                            Text(
                                source,
                                maxLines = 1,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    localSelectedCategories = emptySet()
                    localSelectedSources = emptySet()
                    viewModel.applyFilters(emptyList(), emptyList())
                }
            ) {
                Text(stringResource(R.string.clear_all))
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.applyFilters(
                        localSelectedCategories.toList(),
                        localSelectedSources.toList()
                    )
                    onClose()
                }
            ) {
                Text(stringResource(R.string.apply))
            }
        }
    }
}