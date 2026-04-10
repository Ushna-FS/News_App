package com.example.shared.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import me.sample.library.resources.Res
import me.sample.library.resources.connection_restored_msg
import me.sample.library.resources.no_internet_connection
import org.jetbrains.compose.resources.stringResource

@Composable
fun NetworkSnackbarHandler(
    isConnected: Boolean,
    snackbarHostState: SnackbarHostState
) {
    var firstEmission by remember { mutableStateOf(true) }

    val noInternetMessage = stringResource(Res.string.no_internet_connection)
    val restoredMessage = stringResource(Res.string.connection_restored_msg)

    LaunchedEffect(isConnected) {

        if (firstEmission) {
            firstEmission = false
            return@LaunchedEffect
        }

        snackbarHostState.currentSnackbarData?.dismiss()

        if (!isConnected) {
            while (true) {
                snackbarHostState.showSnackbar(
                    message = noInternetMessage,
                    duration = SnackbarDuration.Short
                )
                delay(6000)
            }
        } else {
            snackbarHostState.showSnackbar(
                message = restoredMessage,
                duration = SnackbarDuration.Short
            )
        }
    }
}