package com.example.newsapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.newsapp.R
import com.example.newsapp.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    var startAnimation by remember { mutableStateOf(false) }

    val offsetX by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -600f,
        animationSpec = tween(800, easing = EaseOutBounce)
    )

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.9f,
        animationSpec = tween(800)
    )

    val auth = FirebaseAuth.getInstance()
    LaunchedEffect(Unit) {

        startAnimation = true

        delay(2500)

        if (auth.currentUser != null) {
            navController.navigate(Routes.MainTabs.route) {
                popUpTo(Routes.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { translationX = offsetX }
                .scale(scale)
        ) {

            Image(
                painter = painterResource(R.drawable.news_icon),
                contentDescription = null,
                modifier = Modifier.size(135.dp, 125.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = (stringResource(R.string.newsmate)),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}