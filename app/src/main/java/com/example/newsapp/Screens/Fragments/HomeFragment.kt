package com.example.newsapp.Screens.Fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
        fetchNews()

        binding.textWelcome.text = getString(R.string.home_user)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(emptyList()) { article ->
            // Handle article click (open details, bookmark, etc.)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
            setHasFixedSize(true)
        }
    }

    private fun fetchNews() {
        lifecycleScope.launch {
            newsViewModel.fetchTopHeadlines()
            newsViewModel.fetchTechCrunchHeadlines()
        }
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
                    recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
                }
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
                    textEmpty.text =getString( R.string.no_articles_found)
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