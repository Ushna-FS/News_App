package com.example.newsapp.Screens.Activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Disable back press properly
        onBackPressedDispatcher.addCallback(this) {
            // Do nothing (back disabled during splash)
        }

        // Start animation immediately
        startAnimations()

        lifecycleScope.launch {
            delay(3000L)
            navigateToMain()
        }
    }

    private fun startAnimations() {

        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        binding.ivNewsIcon.startAnimation(logoAnim)

        val textAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        binding.tvAppName.startAnimation(textAnim)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        finish()
    }
}