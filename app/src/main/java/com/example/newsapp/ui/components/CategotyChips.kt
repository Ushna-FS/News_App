package com.example.newsapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.newsapp.NewsAppTheme
import com.example.shared.utils.Category

@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {

        categories.forEach { category ->

            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                modifier = Modifier.padding(end = 8.dp)
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryChipsPreview() {
    NewsAppTheme {
        var selected by remember { mutableStateOf("Technology") }

        // Use the enum helper
        val categories = Category.names

        CategoryChips(
            categories = categories,
            selectedCategory = selected,
            onCategorySelected = { selected = it }
        )
    }
}