package com.example.newsapp

import android.annotation.SuppressLint
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.Screens.Fragments.FilterFragment
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.ActivityMainBinding
import com.example.newsapp.ViewModels.NewsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var newsAdapter: NewsAdapter
    private val newsViewModel: NewsViewModel by viewModels()
    private var filterFragment: FilterFragment? = null
    private var isFilterOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchFunctionality()
        setupObservers()
        setupBackPressHandler()
        setupFilterButton()
        fetchNews()
        setupSortButton()
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
        newsAdapter = NewsAdapter(emptyList())
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
                    val imm =
                        getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
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

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            if (!isFilterOpen) {
                openFilterPanel()
            } else {
                closeFilterPanel()
            }
        }
    }

    private fun openFilterPanel() {
        filterFragment = FilterFragment().apply {
            setFilterListener(object : FilterFragment.FilterListener {
                override fun onFiltersApplied() {
                    updateFilterSummary()
                }

                override fun onCloseFilter() {
                    closeFilterPanel()
                }
            })
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.filterContainer, filterFragment!!)
            .commit()

        isFilterOpen = true
        binding.btnFilter.text = "Close Filter"
        binding.btnSort.visibility = View.GONE
    }

    private fun closeFilterPanel() {
        filterFragment?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
        filterFragment = null
        isFilterOpen = false
        binding.btnFilter.text = "Filter"
        binding.btnSort.visibility = View.VISIBLE
    }

    private fun fetchNews() {
        newsViewModel.fetchTopHeadlines()
        newsViewModel.fetchTechCrunchHeadlines()
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        lifecycleScope.launch {
            newsViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observe FILTERED news data
        lifecycleScope.launch {
            newsViewModel.filteredNews.collect { articles ->
                newsAdapter = NewsAdapter(articles)
                binding.recyclerView.adapter = newsAdapter

                updateFilterSummary()

                // Show/hide empty state
                if (articles.isEmpty()) {
                    binding.llEmptyState.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE

                    val query = binding.etSearch.text.toString()
                    if (query.isNotEmpty()) {
                        // Use the explicit TextView ID
                        binding.tvEmptyState.text = "No results for '$query'"
                    } else {
                        binding.tvEmptyState.text = "No articles found with current filters"
                    }
                } else {
                    binding.llEmptyState.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }


        // Observe filter changes
        lifecycleScope.launch {
            newsViewModel.selectedCategories.collect { categories ->
                updateFilterSummary()
            }
        }

        lifecycleScope.launch {
            newsViewModel.selectedSources.collect { sources ->
                updateFilterSummary()
            }
        }

        // Observe search query
        lifecycleScope.launch {
            newsViewModel.searchQuery.collect { query ->
                if (query.isNotEmpty()) {
                    binding.toolbar.title = "Search: $query"
                } else {
                    updateFilterSummary()
                }
            }
        }

        // Observe errors
        lifecycleScope.launch {
            newsViewModel.errorMessage.collect { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observe sort type
        lifecycleScope.launch {
            newsViewModel.sortType.collect {
                updateSortButtonHighlight()
            }
        }

        // Observe hasUserSelectedSort
        lifecycleScope.launch {
            newsViewModel.hasUserSelectedSort.collect {
                updateSortButtonHighlight()
            }
        }
    }

    private fun updateFilterButtonHighlight() {
        val filterSummary = newsViewModel.getFilterSummary()
        val isFilterActive = filterSummary != "All News"

        if (isFilterActive) {
            // Change to FILLED button style when filters are active
            binding.btnFilter.apply {
                setTextColor(resources.getColor(R.color.white, theme))
                iconTint = getColorStateList(R.color.white)
                strokeColor = getColorStateList(R.color.blueMain)
                setBackgroundColor(resources.getColor(R.color.blueMain, theme))
            }
        } else {
            // Change back to OUTLINED button style when no filters
            binding.btnFilter.apply {
                setTextColor(resources.getColor(R.color.blueMain, theme))
                iconTint = getColorStateList(R.color.blueMain)
                strokeColor = getColorStateList(R.color.blueMain)
                setBackgroundColor(resources.getColor(android.R.color.transparent, theme))

                // Change back to original icon
                icon = resources.getDrawable(R.drawable.ic_filter, theme)
            }
        }
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener {
            showSortMenu()
        }
    }

    private fun updateSortButtonHighlight() {
        val hasUserSelected = newsViewModel.hasUserSelectedSort.value

        if (hasUserSelected) {
            // Highlight for ANY user selection
            binding.btnSort.apply {
                setTextColor(resources.getColor(R.color.white, theme))
                iconTint = getColorStateList(R.color.white)
                strokeColor = getColorStateList(R.color.blueMain)
                setBackgroundColor(resources.getColor(R.color.blueMain, theme))

                text = when (newsViewModel.sortType.value) {
                    NewsViewModel.SortType.NEWEST_FIRST -> "Newest"
                    NewsViewModel.SortType.OLDEST_FIRST -> "Oldest"
                    else -> "Sort By"
                }
            }
        } else {
            // Default (no user selection yet)
            binding.btnSort.apply {
                setTextColor(resources.getColor(R.color.blueMain, theme))
                iconTint = getColorStateList(R.color.blueMain)
                strokeColor = getColorStateList(R.color.blueMain)
                setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
                text = "Sort By"
            }
        }
    }

    // sort menu
    private fun showSortMenu() {
        val items = arrayOf("Newest First", "Oldest First", "Reset to Default")

        MaterialAlertDialogBuilder(this)
            .setTitle("Sort Articles")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> newsViewModel.setSortType(NewsViewModel.SortType.NEWEST_FIRST)
                    1 -> newsViewModel.setSortType(NewsViewModel.SortType.OLDEST_FIRST)
                    2 -> newsViewModel.resetSort()  // Reset to default
                }
                updateSortButtonHighlight()
                updateFilterSummary()
            }
            .show()
    }

    private fun updateFilterSummary() {
        val filterSummary = newsViewModel.getFilterSummary()
        val totalArticles = newsViewModel.filteredNews.value.size

        // Update toolbar title
        if (binding.etSearch.text.isNullOrEmpty()) {
            binding.toolbar.title = "$filterSummary ($totalArticles articles)"
        } else {
            binding.toolbar.title = "Search: '${binding.etSearch.text}' ($totalArticles articles)"
        }

        // UPDATE THE TEXTVIEW BELOW FILTER BUTTONS
        if (totalArticles > 0) {
            binding.tvFilterSummary.text = "Showing: $filterSummary • $totalArticles articles"
            binding.tvFilterSummary.visibility = View.VISIBLE
        } else {
            binding.tvFilterSummary.text = "No articles found for: $filterSummary"
            binding.tvFilterSummary.visibility = View.VISIBLE
        }

        updateFilterButtonHighlight()
        updateSortButtonHighlight()
    }
}