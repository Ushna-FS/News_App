package com.example.newsapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newsapp.data.repository.SortType


@Composable
fun SortMenuDialog(
    currentSortType: SortType,
    hasUserSelected: Boolean,
    onSortSelected: (SortType, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Articles") },
        text = {
            Column {
                SortOption(
                    label = "Newest First",
                    isSelected = currentSortType == SortType.NEWEST_FIRST && hasUserSelected,
                    onClick = {
                        onSortSelected(SortType.NEWEST_FIRST, true)
                    }
                )
                SortOption(
                    label = "Oldest First",
                    isSelected = currentSortType == SortType.OLDEST_FIRST && hasUserSelected,
                    onClick = {
                        onSortSelected(SortType.OLDEST_FIRST, true)
                    }
                )
                SortOption(
                    label = "Reset to Default",
                    isSelected = !hasUserSelected,
                    onClick = {
                        onSortSelected(SortType.NEWEST_FIRST, false)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SortOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}