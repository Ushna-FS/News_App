package com.example.newsapp.Screens.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentHomeBinding
import com.example.newsapp.ViewModels.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val newsViewModel: NewsViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupScrollListener()
        fetchNews()

        binding.textWelcome.text = getString(R.string.home_user)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(
            articles = emptyList(),
            onItemClick = { article ->
                // Handle article click
            },
            onLoadMore = {}  // ✅ Empty - fragment handles it
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupScrollListener() {
        var loadingTriggered = false  // ✅ Prevent duplicate triggers

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy <= 0) return  // ✅ Only trigger on scrolling down

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItem = firstVisibleItem + visibleItemCount

                // ✅ Load more when reaching 5th last item
                val shouldLoadMore = lastVisibleItem >= totalItemCount - 5

                if (shouldLoadMore &&
                    !newsViewModel.isLoadingMore.value &&
                    !newsViewModel.isLoading.value &&
                    newsViewModel.hasMorePages.value &&
                    !loadingTriggered) {

                    loadingTriggered = true
                    newsViewModel.loadMoreNews()

                    // ✅ Reset after delay
                    recyclerView.postDelayed({
                        loadingTriggered = false
                    }, 4000)  // 4 seconds (loader duration)
                }
            }
        })
    }
    // ✅ NEW: Add scroll listener for better pagination control
//    private fun setupScrollListener() {
//        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                val visibleItemCount = layoutManager.childCount
//                val totalItemCount = layoutManager.itemCount
//                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
//
//                // Load more when reaching the end
//                if (!newsViewModel.isLoadingMore.value &&
//                    (visibleItemCount + firstVisibleItem) >= totalItemCount - 5) {
//                    newsViewModel.loadMoreNews()
//                }
//            }
//        })
//    }

    private fun fetchNews() {
        newsViewModel.fetchTopHeadlines()
        newsViewModel.fetchTechCrunchHeadlines()
    }

    private fun setupObservers() {
        // Observe news data
        lifecycleScope.launch {
            newsViewModel.filteredNews.collect { articles ->
                newsAdapter.updateArticles(articles)
                updateEmptyState(articles)
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            newsViewModel.isLoading.collect { isLoading ->
                binding.apply {
                    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    if (isLoading) {
                        recyclerView.visibility = View.GONE
                    }
                }
            }
        }

        // ✅ NEW: Observe loading more state
        lifecycleScope.launch {
            newsViewModel.isLoadingMore.collect { isLoadingMore ->
                newsAdapter.setLoading(isLoadingMore)
            }
        }
        lifecycleScope.launch {
            newsViewModel.hasMorePages.collect { hasMore ->
                newsAdapter.setHasMorePages(hasMore)
            }
        }
        // Observe errors
        lifecycleScope.launch {
            newsViewModel.errorMessage.collect { error ->
                if (error.isNotEmpty()) {
                    binding.textEmpty.text = getString(R.string.error, error)
                    binding.textEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateEmptyState(articles: List<com.example.newsapp.Data.models.Article>) {
        binding.apply {
            when {
                articles.isEmpty() -> {
                    textEmpty.text = getString(R.string.no_articles_found)
                    textEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                else -> {
                    textEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}