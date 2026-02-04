package com.example.newsapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.newsapp.screens.fragments.BookmarksFragment
import com.example.newsapp.screens.fragments.HomeFragment
import com.example.newsapp.databinding.ActivityMainBinding
import com.example.newsapp.screens.fragments.DiscoverFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Fragments
    private val homeFragment by lazy { HomeFragment() }
    private val discoverFragment by lazy { DiscoverFragment() }
    private val bookmarksFragment by lazy { BookmarksFragment() }
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        showFragment(homeFragment)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    showFragment(homeFragment)
                    true
                }

                R.id.discover -> {
                    showFragment(discoverFragment)
                    true
                }

                R.id.bookmarks -> {
                    showFragment(bookmarksFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        // Don't replace if it's already showing
        if (currentFragment == fragment) return

        supportFragmentManager.beginTransaction().apply {
            // Add animation only if files exist
            try {
                setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            } catch (e: Exception) {
                // If animation files don't exist, continue without animation
            }

            // Hide current fragment
            currentFragment?.let { hide(it) }

            // Show new fragment
            if (fragment.isAdded) {
                show(fragment)
            } else {
                add(R.id.fragment_container, fragment, fragment::class.java.simpleName)
            }

            commit()
        }

        currentFragment = fragment
    }

    override fun onBackPressed() {
        // If not on home, go to home
        if (currentFragment != homeFragment) {
            binding.bottomNavigation.selectedItemId = R.id.home
        } else {
            super.onBackPressed()
        }
    }
}
