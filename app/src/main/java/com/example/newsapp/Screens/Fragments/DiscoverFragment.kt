package com.example.newsapp.Screens.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentDiscoverBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    // 🔥 SAME ViewModel as MainActivity
    private val newsViewModel: NewsViewModel by activityViewModels()

    private lateinit var newsAdapter: NewsAdapter
    private var filterFragment: FilterFragment? = null
    private var isFilterOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchFunctionality()
        setupObservers()
        setupBackPressHandler()
        setupFilterButton()
        setupSortButton()
        fetchNews()
    }

    // ---------------- RecyclerView ----------------

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(emptyList())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    // ---------------- Search ----------------

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
                hideKeyboard()
                binding.etSearch.clearFocus()
                true
            } else false
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

    // ---------------- Back press ----------------

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!binding.etSearch.text.isNullOrEmpty()) {
                binding.etSearch.text.clear()
                newsViewModel.clearSearch()
                binding.etSearch.requestFocus()
            }
        }
    }

    // ---------------- Filter ----------------

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            if (!isFilterOpen) openFilterPanel() else closeFilterPanel()
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

        childFragmentManager.beginTransaction()
            .replace(R.id.filterContainer, filterFragment!!)
            .commit()

        isFilterOpen = true
        binding.btnFilter.text = "Close Filter"
        binding.btnSort.visibility = View.GONE
    }

    private fun closeFilterPanel() {
        filterFragment?.let {
            childFragmentManager.beginTransaction().remove(it).commit()
        }
        filterFragment = null
        isFilterOpen = false
        binding.btnFilter.text = "Filter"
        binding.btnSort.visibility = View.VISIBLE
    }

    // ---------------- Sort ----------------

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener { showSortMenu() }
    }

    private fun showSortMenu() {
        val items = arrayOf("Newest First", "Oldest First", "Reset to Default")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort Articles")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> newsViewModel.setSortType(NewsViewModel.SortType.NEWEST_FIRST)
                    1 -> newsViewModel.setSortType(NewsViewModel.SortType.OLDEST_FIRST)
                    2 -> newsViewModel.resetSort()
                }
                updateSortButtonHighlight()
                updateFilterSummary()
            }
            .show()
    }

    // ---------------- Data ----------------

    private fun fetchNews() {
        newsViewModel.fetchTopHeadlines()
        newsViewModel.fetchTechCrunchHeadlines()
    }

    // ---------------- Observers (IDENTICAL LOGIC) ----------------

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {

        lifecycleScope.launch {
            newsViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility =
                    if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            newsViewModel.filteredNews.collect { articles ->

                newsAdapter = NewsAdapter(articles)
                binding.recyclerView.adapter = newsAdapter

                updateFilterSummary()

                binding.apply {
                    if (articles.isEmpty()) {
                        llEmptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE

                        val query = etSearch.text.toString()
                        tvEmptyState.text = if (query.isNotEmpty()) {
                            "No results for '$query'"
                        } else {
                            "No articles found with current filters"
                        }
                    } else {
                        llEmptyState.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }

        lifecycleScope.launch {
            newsViewModel.selectedCategories.collect {
                updateFilterSummary()
            }
        }

        lifecycleScope.launch {
            newsViewModel.selectedSources.collect {
                updateFilterSummary()
            }
        }

        lifecycleScope.launch {
            newsViewModel.searchQuery.collect { query ->
                if (query.isNotEmpty()) {
                    binding.toolbar.title = "Search: $query"
                } else {
                    updateFilterSummary()
                }
            }
        }

        lifecycleScope.launch {
            newsViewModel.errorMessage.collect { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            }
        }

        lifecycleScope.launch {
            newsViewModel.sortType.collect {
                updateSortButtonHighlight()
            }
        }

        lifecycleScope.launch {
            newsViewModel.hasUserSelectedSort.collect {
                updateSortButtonHighlight()
            }
        }
    }

    // ---------------- UI Highlighting (SAME AS ACTIVITY) ----------------

    private fun updateFilterButtonHighlight() {
        val isFilterActive = newsViewModel.getFilterSummary() != "All News"

        if (isFilterActive) {
            binding.btnFilter.apply {
                setTextColor(resources.getColor(R.color.white, requireContext().theme))
                iconTint = resources.getColorStateList(R.color.white, requireContext().theme)
                strokeColor = resources.getColorStateList(R.color.blueMain, requireContext().theme)
                setBackgroundColor(resources.getColor(R.color.blueMain, requireContext().theme))
            }
        } else {
            binding.btnFilter.apply {
                setTextColor(resources.getColor(R.color.blueMain, requireContext().theme))
                iconTint = resources.getColorStateList(R.color.blueMain, requireContext().theme)
                strokeColor = resources.getColorStateList(R.color.blueMain, requireContext().theme)
                setBackgroundColor(resources.getColor(android.R.color.transparent, requireContext().theme))
                icon = resources.getDrawable(R.drawable.ic_filter, requireContext().theme)
            }
        }
    }

    private fun updateSortButtonHighlight() {
        val hasUserSelected = newsViewModel.hasUserSelectedSort.value

        if (hasUserSelected) {
            binding.btnSort.apply {
                setTextColor(resources.getColor(R.color.white, requireContext().theme))
                iconTint = resources.getColorStateList(R.color.white, requireContext().theme)
                strokeColor = resources.getColorStateList(R.color.blueMain, requireContext().theme)
                setBackgroundColor(resources.getColor(R.color.blueMain, requireContext().theme))

                text = when (newsViewModel.sortType.value) {
                    NewsViewModel.SortType.NEWEST_FIRST -> "Newest"
                    NewsViewModel.SortType.OLDEST_FIRST -> "Oldest"
                    else -> "Sort By"
                }
            }
        } else {
            binding.btnSort.apply {
                setTextColor(resources.getColor(R.color.blueMain, requireContext().theme))
                iconTint = resources.getColorStateList(R.color.blueMain, requireContext().theme)
                strokeColor = resources.getColorStateList(R.color.blueMain, requireContext().theme)
                setBackgroundColor(resources.getColor(android.R.color.transparent, requireContext().theme))
                text = "Sort By"
            }
        }
    }

    private fun updateFilterSummary() {
        val filterSummary = newsViewModel.getFilterSummary()
        val totalArticles = newsViewModel.filteredNews.value.size

        if (binding.etSearch.text.isNullOrEmpty()) {
            binding.toolbar.title = "$filterSummary ($totalArticles articles)"
        } else {
            binding.toolbar.title =
                "Search: '${binding.etSearch.text}' ($totalArticles articles)"
        }

        binding.tvFilterSummary.text =
            if (totalArticles > 0) {
                "Showing: $filterSummary • $totalArticles articles"
            } else {
                "No articles found for: $filterSummary"
            }

        binding.tvFilterSummary.visibility = View.VISIBLE

        updateFilterButtonHighlight()
        updateSortButtonHighlight()
    }

    // ---------------- Utils ----------------

    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}