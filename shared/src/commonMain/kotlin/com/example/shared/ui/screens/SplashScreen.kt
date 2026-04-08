package com.example.shared.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shared.navigation.Routes
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay
import me.sample.library.resources.Res
import me.sample.library.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


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

    val auth = Firebase.auth
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)

        if (auth.currentUser != null) {
            navController.navigate(Routes.MainTabs) {
                popUpTo(Routes.Splash) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate(Routes.Login) {
                popUpTo(Routes.Splash) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    SplashContent(
        offsetX = offsetX,
        scale = scale
    )
}

@Composable
fun SplashContent(
    offsetX: Float,
    scale: Float
) {
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
                painter = painterResource(Res.drawable.news_icon),
                contentDescription = null,
                modifier = Modifier.size(135.dp, 125.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = stringResource(Res.string.newsmate),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashContentPreview() {
    SplashContent(
        offsetX = 0f,   // final position
        scale = 1f      // fully scaled
    )
}