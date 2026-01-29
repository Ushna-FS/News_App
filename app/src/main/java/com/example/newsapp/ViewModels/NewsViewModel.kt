package com.example.newsapp.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.Data.models.NewsResponse
import com.example.newsapp.Data.Repository.NewsRepository
import com.example.newsapp.Data.models.Article
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val repository = NewsRepository()

    // LiveData for individual sources
    private val _businessNews = MutableLiveData<NewsResponse>()
    val businessNews: LiveData<NewsResponse> get() = _businessNews

    private val _techCrunchNews = MutableLiveData<NewsResponse>()
    val techCrunchNews: LiveData<NewsResponse> get() = _techCrunchNews

    // Combined LiveData for display
    private val _allNewsLiveData = MutableLiveData<List<Article>>()
    val allNewsLiveData: LiveData<List<Article>> get() = _allNewsLiveData

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Search query
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> get() = _searchQuery

    // Search job for debounce
    private var searchJob: Job? = null

    // Track if we've fetched both sources
    private var businessFetched = false
    private var techCrunchFetched = false

    fun fetchTopHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getTopHeadlines()
                if (response.isSuccessful && response.body() != null) {
                    _businessNews.value = response.body()!!
                    businessFetched = true
                    combineNewsIfReady()
                } else {
                    _errorMessage.value = "Failed to fetch business news"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Business Error: ${e.message}"
            }
        }
    }

    fun fetchTechCrunchHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getTechCrunchHeadlines()
                if (response.isSuccessful && response.body() != null) {
                    _techCrunchNews.value = response.body()!!
                    techCrunchFetched = true
                    combineNewsIfReady()
                } else {
                    _errorMessage.value = "Failed to fetch TechCrunch news"
                }
            } catch (e: Exception) {
                _errorMessage.value = "TechCrunch Error: ${e.message}"
            }
        }
    }

    private fun combineNewsIfReady() {
        if (businessFetched && techCrunchFetched) {
            val businessArticles = _businessNews.value?.articles ?: emptyList()
            val techArticles = _techCrunchNews.value?.articles ?: emptyList()

            // Combine and sort by latest
            val combined = (businessArticles + techArticles).sortedByDescending {
                it.publishedAt
            }

            _allNewsLiveData.value = combined
            _isLoading.value = false
        }
    }

    // Search news function
    fun searchNews(query: String) {
        _searchQuery.value = query

        searchJob?.cancel()

        if (query.isEmpty()) {
            // When search is cleared, show combined news again
            combineNewsIfReady()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)

            _isLoading.value = true
            try {
                val response = repository.searchNews(query)
                if (response.isSuccessful && response.body() != null) {
                    _allNewsLiveData.value = response.body()!!.articles
                } else {
                    _errorMessage.value = "No results found for '$query'"
                    _allNewsLiveData.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search error: ${e.message}"
                _allNewsLiveData.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        combineNewsIfReady()
    }
}