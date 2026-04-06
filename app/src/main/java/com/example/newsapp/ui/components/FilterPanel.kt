package com.example.newsapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsapp.R
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.shared.utils.Category

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

    val selectedCategoryStrings by viewModel.selectedCategories.collectAsStateWithLifecycle()


    var localSelectedSources by remember { mutableStateOf(selectedSources.toSet()) }

    // Convert to Set<Category> for UI
    var localSelectedCategories by remember {
        mutableStateOf(selectedCategoryStrings.mapNotNull { str ->
            Category.values().find { it.displayName == str }
        }.toSet())
    }

    // Update local state when ViewModel state changes
    LaunchedEffect(selectedCategoryStrings, selectedSources) {
        localSelectedCategories = selectedCategoryStrings.mapNotNull { str ->
            Category.values().find { it.displayName == str }
        }.toSet()
        localSelectedSources = selectedSources.toSet()
    }

    val categories = Category.list

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
                    label = { Text(category.displayName) }  // enum display name
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
                        localSelectedCategories.map { it.displayName },
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