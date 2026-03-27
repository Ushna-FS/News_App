package com.example.newsapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.navigation.RootNavigation
import com.example.newsapp.ui.theme.LightColors
import com.example.newsapp.viewmodels.NetworkViewModel
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {

            val networkViewModel: NetworkViewModel = hiltViewModel()
            val isConnected by networkViewModel.isConnected.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            var firstEmission by remember { mutableStateOf(true) }

            LaunchedEffect(isConnected) {

                if (firstEmission) {
                    firstEmission = false
                    return@LaunchedEffect
                }

                snackbarHostState.currentSnackbarData?.dismiss()

                if (!isConnected) {

                    while (true) {

                        snackbarHostState.showSnackbar(
                            message = getString(R.string.no_internet_connection),
                            duration = SnackbarDuration.Short
                        )

                        delay(6000) // show again after 6 seconds
                    }

                } else {

                    snackbarHostState.showSnackbar(
                        message = getString(R.string.connection_restored_msg),
                        duration = SnackbarDuration.Short
                    )
                }
            }
            NewsAppTheme {

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        ) { data ->

                            val isOffline =
                                data.visuals.message == getString(R.string.no_internet_connection)

                            Snackbar(
                                snackbarData = data,
                                containerColor = if (isOffline) LightColors.error else LightColors.tertiary,
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