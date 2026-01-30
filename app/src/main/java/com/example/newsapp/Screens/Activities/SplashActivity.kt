package com.example.newsapp.Screens.Activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivitySplashBinding
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_DURATION: Long = 5000  // 5 seconds
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start bounce animation
        val anim = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        binding.ivNewsIcon.startAnimation(anim)

        // Navigate to MainActivity after 5 seconds using coroutines
        navigateToHome()
    }

    private fun navigateToHome() {
        activityScope.launch {
            delay(SPLASH_DURATION)  // Suspend for 5 seconds

            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()  // Remove splash from back stack
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel coroutines when activity is destroyed to prevent memory leaks
        activityScope.cancel()
    }
}