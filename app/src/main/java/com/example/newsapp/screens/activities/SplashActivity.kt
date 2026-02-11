package com.example.newsapp.screens.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        // Install splash BEFORE super
        installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Disable back press
        onBackPressedDispatcher.addCallback(this) {}

        startAnimations()

        // Coroutine-based delay (non-blocking)
        lifecycleScope.launch {
            delay(2500L)   // match animation duration
            navigateToMain()
        }
    }


    private fun startAnimations() {
        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        val textAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)

        binding.ivNewsIcon.startAnimation(logoAnim)
        binding.tvAppName.startAnimation(textAnim)
    }

    private fun navigateToMain() {

        val intent = Intent(this, MainActivity::class.java)

        val options = ActivityOptions.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        startActivity(intent, options.toBundle())
        finish()
    }
}

//@AndroidEntryPoint
//class SplashActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivitySplashBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        installSplashScreen()
//
//        super.onCreate(savedInstanceState)
//
//        binding = ActivitySplashBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Disable back press during splash
//        onBackPressedDispatcher.addCallback(this) {}
//
//        startAnimations()
//
//        lifecycleScope.launch {
//            delay(3000L)
//            navigateToMain()
//        }
//    }
//
//    private fun startAnimations() {
//        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
//        val textAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
//
//        binding.ivNewsIcon.startAnimation(logoAnim)
//        binding.tvAppName.startAnimation(textAnim)
//    }
//
//    private fun navigateToMain() {
//        startActivity(Intent(this, MainActivity::class.java))
//
//        val options = ActivityOptions.makeCustomAnimation(
//            this,
//            android.R.anim.fade_in,
//            android.R.anim.fade_out
//        )
//        finish()
//    }
//}