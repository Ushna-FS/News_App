package com.example.newsapp

import android.os.Bundle
import android.widget.Toast
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
        setupObservers()
        fetchNews()
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = newsAdapter
    }

    private fun setupObservers() {
        // Observe loading state
        newsViewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        })

        // Observe news data
        newsViewModel.newsLiveData.observe(this, Observer { newsResponse ->
            // Update RecyclerView adapter with articles
            newsAdapter = NewsAdapter(newsResponse.articles)
            binding.recyclerView.adapter = newsAdapter

            Toast.makeText(this, "Fetched ${newsResponse.articles.size} articles", Toast.LENGTH_SHORT).show()
        })

        // Observe errors
        newsViewModel.errorMessage.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })
    }

    private fun fetchNews() {
        newsViewModel.fetchTopHeadlines()
    }
}