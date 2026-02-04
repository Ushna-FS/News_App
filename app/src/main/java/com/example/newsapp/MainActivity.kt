package com.example.newsapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.newsapp.screens.fragments.*
import com.example.newsapp.databinding.ActivityMainBinding
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
                    if (!isArticleDetailShowing()) {
                        showFragment(homeFragment)
                    }
                    true
                }

                R.id.discover -> {
                    if (!isArticleDetailShowing()) {
                        showFragment(discoverFragment)
                    }
                    true
                }

                R.id.bookmarks -> {
                    if (!isArticleDetailShowing()) {
                        showFragment(bookmarksFragment)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun isArticleDetailShowing(): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        return currentFragment is ArticleDetailFragment
    }

    private fun showFragment(fragment: Fragment) {
        // Don't replace if it's already showing
        if (currentFragment == fragment && !isArticleDetailShowing()) return

        supportFragmentManager.beginTransaction().apply {
            // Clear back stack if we're switching bottom nav tabs
            if (!isArticleDetailShowing()) {
                supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

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

            replace(R.id.fragment_container, fragment) // Always use REPLACE
            if (!isArticleDetailShowing()) {
                // Only add to back stack if not coming from ArticleDetail
                addToBackStack(fragment::class.java.simpleName)
            }
            commit()
        }

        currentFragment = fragment
    }

    override fun onBackPressed() {
        // If ArticleDetailFragment is showing, pop it
        if (isArticleDetailShowing()) {
            supportFragmentManager.popBackStack()
        }
        // If there are fragments in back stack from bottom navigation, pop them
        else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
        // If not on home and back stack is empty, go to home
        else if (currentFragment != homeFragment) {
            binding.bottomNavigation.selectedItemId = R.id.home
        } else {
            super.onBackPressed()
        }
    }
}