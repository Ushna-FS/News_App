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
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        showFragment(HomeFragment())
        onBackPressedDispatcher.addCallback(
            this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPress()
                }
            }
        )


    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    if (!isArticleDetailShowing()) {
                        showFragment(HomeFragment())
                    }
                    true
                }

                R.id.discover -> {
                    if (!isArticleDetailShowing()) {
                        showFragment(DiscoverFragment())
                    }
                    true
                }

                R.id.bookmarks -> {
                    if (!isArticleDetailShowing()) {
                        showFragment(BookmarksFragment())
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
        supportFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .commit()

        currentFragment = fragment
    }

    private fun handleBackPress() {
        when {
            isArticleDetailShowing() -> {
                supportFragmentManager.popBackStack()
            }

            currentFragment !is HomeFragment -> {
                binding.bottomNavigation.selectedItemId = R.id.home
            }

            else -> finish()
        }
    }
}