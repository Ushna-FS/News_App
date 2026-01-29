package com.example.newsapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.ActivityMainBinding
import com.example.newsapp.ViewModels.NewsViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var newsAdapter: NewsAdapter
    private val newsViewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchFunctionality()
        setupObservers()
        setupBackPressHandler()
        fetchNews()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!binding.etSearch.text.isNullOrEmpty()) {
                    binding.etSearch.text.clear()
                    newsViewModel.clearSearch()
                    binding.etSearch.requestFocus()
                } else {
                    finish()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = newsAdapter
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.ivClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                newsViewModel.searchNews(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ivClear.setOnClickListener {
            binding.etSearch.text.clear()
            newsViewModel.clearSearch()
            binding.etSearch.requestFocus()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    newsViewModel.searchNews(query)
                    binding.etSearch.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                }
                true
            } else {
                false
            }
        }

        binding.etSearch.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (!binding.etSearch.text.isNullOrEmpty()) {
                    binding.etSearch.text.clear()
                    newsViewModel.clearSearch()
                    return@setOnKeyListener true
                }
            }
            false
        }
    }

    private fun fetchNews() {
        newsViewModel.fetchTopHeadlines()
        newsViewModel.fetchTechCrunchHeadlines()
    }

    private fun setupObservers() {
        // Observe loading state
        newsViewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        // Observe COMBINED news data (Business + TechCrunch)
        newsViewModel.allNewsLiveData.observe(this, Observer { articles ->
            newsAdapter = NewsAdapter(articles)
            binding.recyclerView.adapter = newsAdapter

            // Show/hide empty state
            if (articles.isEmpty()) {
                binding.llEmptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE

                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    val emptyStateText = binding.llEmptyState.getChildAt(1) as TextView
                    emptyStateText.text = "No results for '$query'"
                } else {
                    val emptyStateText = binding.llEmptyState.getChildAt(1) as TextView
                    emptyStateText.text = "No articles found"
                }
            } else {
                binding.llEmptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE

                // Update title with counts
                updateToolbarTitle(articles.size)
            }
        })

        // Observe individual sources to show counts
        newsViewModel.businessNews.observe(this, Observer { businessResponse ->
            newsViewModel.techCrunchNews.value?.let { techResponse ->
                val total = (businessResponse.articles.size + techResponse.articles.size)
                updateToolbarTitle(total)
            }
        })

        newsViewModel.techCrunchNews.observe(this, Observer { techResponse ->
            newsViewModel.businessNews.value?.let { businessResponse ->
                val total = (businessResponse.articles.size + techResponse.articles.size)
                updateToolbarTitle(total)
            }
        })

        // Observe search query
        newsViewModel.searchQuery.observe(this, Observer { query ->
            if (query.isNotEmpty()) {
                binding.toolbar.title = "Search: $query"
            } else {
                // Show default title when no search
                newsViewModel.allNewsLiveData.value?.let { articles ->
                    updateToolbarTitle(articles.size)
                }
            }
        })

        // Observe errors
        newsViewModel.errorMessage.observe(this, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateToolbarTitle(totalArticles: Int) {

        if (binding.etSearch.text.isNullOrEmpty()) {
            binding.toolbar.title = "Latest News"
            Toast.makeText(this, "Loaded $totalArticles articles", Toast.LENGTH_SHORT).show()
        }
    }
}