package com.example.newsapp.screens.fragments

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.adapters.NewsLoadStateAdapter
import com.example.newsapp.adapters.NewsPagingAdapter
import com.example.newsapp.data.Repository.SortType
import com.example.newsapp.databinding.FragmentDiscoverBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private val newsViewModel: NewsViewModel by activityViewModels()
    private lateinit var newsAdapter: NewsPagingAdapter
    private var filterFragment: FilterFragment? = null
    private var isFilterOpen = false

    private var isSearching = false

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
        setupFilterFragment()
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsPagingAdapter(
            onItemClick = { article ->
                // Handle article click
            },
            onExtractSource = { article ->
                newsViewModel.extractSourceFromArticle(article)
            }
        )

        newsAdapter.addLoadStateListener { loadState ->
            binding.apply {
                // Initial load
                when (loadState.refresh) {
                    is LoadState.Loading -> {
                        progressBar.isVisible = true
                        recyclerView.isVisible = false
                        llEmptyState.isVisible = false
                        tvLoadingMore.isVisible = false
                    }
                    is LoadState.Error -> {
                        progressBar.isVisible = false
                        tvLoadingMore.isVisible = false
                        llEmptyState.isVisible = true
                        recyclerView.isVisible = false
                        val error = (loadState.refresh as LoadState.Error).error
                        tvEmptyState.text = "Error: ${error.message}"
                    }
                    else -> {
                        progressBar.isVisible = false
                    }
                }

                // Pagination load
                when (loadState.append) {
                    is LoadState.Loading -> {
                        tvLoadingMore.isVisible = true
                    }
                    else -> {
                        tvLoadingMore.isVisible = false
                    }
                }

                // Empty state
                val isEmpty = loadState.refresh !is LoadState.Loading &&
                        loadState.append.endOfPaginationReached &&
                        newsAdapter.itemCount == 0

                if (isEmpty) {
                    llEmptyState.isVisible = true
                    recyclerView.isVisible = false
                    val query = binding.etSearch.text.toString()
                    tvEmptyState.text = if (query.isNotEmpty()) {
                        "No results for '$query'"
                    } else {
                        "No articles found"
                    }
                } else {
                    llEmptyState.isVisible = false
                    recyclerView.isVisible = true
                }

                // Update summary
                updateFilterSummary()
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter.withLoadStateFooter(
                footer = NewsLoadStateAdapter { newsAdapter.retry() }
            )
            setHasFixedSize(true)
        }
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                binding.ivClear.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
                newsViewModel.searchNews(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ivClear.setOnClickListener {
            binding.etSearch.text.clear()
            newsViewModel.clearSearch()
            hideKeyboard()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                binding.etSearch.clearFocus()
                true
            } else false
        }
    }

    private fun setupObservers() {
        // CORRECTED: Simple flow switching
        viewLifecycleOwner.lifecycleScope.launch {
            // Collect search query
            newsViewModel.searchQuery.collectLatest { query ->
                isSearching = query.isNotEmpty()

                if (query.isNotEmpty()) {
                    // Search mode
                    newsViewModel.searchNewsPagingData.collectLatest { pagingData ->
                        newsAdapter.submitData(pagingData)
                    }
                } else {
                    // Normal mode - show combined news
                    newsViewModel.combinedNewsPagingData.collectLatest { pagingData ->
                        newsAdapter.submitData(pagingData)
                    }
                }
            }
        }

        // Error observer
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.errorMessage.collect { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!binding.etSearch.text.isNullOrEmpty()) {
                binding.etSearch.text.clear()
                newsViewModel.clearSearch()
                hideKeyboard()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateFilterSummary() {
        val filterSummary = newsViewModel.getFilterSummary()
        val query = binding.etSearch.text.toString()
        val itemCount = newsAdapter.itemCount

        if (query.isNotEmpty()) {
            binding.toolbar.title = "Search: '$query' ($itemCount articles)"
        } else {
            binding.toolbar.title = "$filterSummary ($itemCount articles)"
        }

        binding.tvFilterSummary.text = if (itemCount > 0) {
            "Showing: $filterSummary • $itemCount articles"
        } else {
            "No articles found"
        }

        binding.tvFilterSummary.visibility = View.VISIBLE
    }

    private fun setupFilterFragment() {
                filterFragment = childFragmentManager.findFragmentById(R.id.filterContainer) as? FilterFragment
        filterFragment?.setFilterListener(object : FilterFragment.FilterListener {
            override fun onFiltersApplied() {
                // Filters were applied in FilterFragment
                updateFilterSummary()
            }
            override fun onCloseFilter() {
                closeFilterPanel()
            }
        })
    }

    private fun setupFilterButton() {
                binding.btnFilter.setOnClickListener {
            if (!isFilterOpen) openFilterPanel() else closeFilterPanel()
        }
    }

    private fun showSortMenu() {
    val items = arrayOf("Newest First", "Oldest First", "Reset to Default")

    MaterialAlertDialogBuilder(requireContext())
        .setTitle("Sort Articles")
        .setItems(items) { _, which ->
            when (which) {
                0 -> newsViewModel.setSortType(SortType.NEWEST_FIRST)
                1 -> newsViewModel.setSortType(SortType.OLDEST_FIRST)
                2 -> newsViewModel.resetSort()
            }
            updateSortButtonHighlight()
            updateFilterSummary()
        }
        .show()
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
                    SortType.NEWEST_FIRST -> "Newest"
                    SortType.OLDEST_FIRST -> "Oldest"
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
    private fun closeFilterPanel() {
        filterFragment?.let {
            childFragmentManager.beginTransaction().remove(it).commit()
        }
        filterFragment = null
        isFilterOpen = false
        binding.btnFilter.text = "Filter"
        binding.btnSort.visibility = View.VISIBLE
    }

    private fun openFilterPanel() {
                filterFragment = FilterFragment().apply {
            setFilterListener(object : FilterFragment.FilterListener {
                override fun onFiltersApplied() {
                    // Refresh data when filters are applied
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
    private fun setupSortButton() {
                binding.btnSort.setOnClickListener { showSortMenu() }
    }
    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}