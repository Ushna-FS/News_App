package com.example.newsapp.Screens.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_DURATION: Long = 5000  // 5 seconds
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySplashBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Start bounce animation
    val anim = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
    binding.ivNewsIcon.startAnimation(anim)

    // Navigate to MainActivity after 5 seconds
    navigateToHome()
}

    private fun navigateToHome() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Remove splash from back stack
        }, SPLASH_DURATION)
    }
}