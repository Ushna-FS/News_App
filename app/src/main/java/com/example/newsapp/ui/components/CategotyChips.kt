package com.example.newsapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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


//package com.example.newsapp.ui.components
//
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun CategoryChips(
//    selected: String,
//    onSelected: (String) -> Unit
//) {
//
//    val categories = listOf(
//        "All",
//        "Business",
//        "Technology",
//        "Sports",
//        "Health"
//    )
//
//    Row(
//        modifier = Modifier
//            .horizontalScroll(rememberScrollState())
//            .padding(vertical = 12.dp)
//    ) {
//
//        categories.forEach { category ->
//
//            FilterChip(
//                selected = selected == category,
//                onClick = { onSelected(category) },
//                label = { Text(category) },
//                modifier = Modifier.padding(end = 8.dp)
//            )
//
//        }
//    }
//}