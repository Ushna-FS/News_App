package com.example.newsapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.shared.NewsAppTheme
import com.example.shared.navigation.RootNavigation
import com.example.shared.ui.theme.LightColors
import com.example.shared.utils.NetworkSnackbarHandler
import com.example.shared.viewmodels.NetworkViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val networkViewModel: NetworkViewModel = koinViewModel()
            val isConnected by networkViewModel.isConnected.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            // network shared logic
            NetworkSnackbarHandler(
                isConnected = isConnected,
                snackbarHostState = snackbarHostState
            )
            NewsAppTheme {

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        ) { data ->
                            // Determine color based on snackbar type
                            val containerColor = when {
                                data.visuals.message.contains(
                                    "no internet",
                                    ignoreCase = true
                                ) -> LightColors.error

                                data.visuals.message.contains(
                                    "restored",
                                    ignoreCase = true
                                ) -> LightColors.tertiary

                                else -> LightColors.tertiary
                            }

                            Snackbar(
                                snackbarData = data,
                                containerColor = containerColor,
                                contentColor = Color.White
                            )
                        }
                    }
                ) {
                    RootNavigation()
                }
            }
        }

    }

}