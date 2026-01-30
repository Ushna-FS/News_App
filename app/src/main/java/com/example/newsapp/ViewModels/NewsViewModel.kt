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

    // LiveData for filtered news
    private val _filteredNewsLiveData = MutableLiveData<List<Article>>()
    val filteredNewsLiveData: LiveData<List<Article>> get() = _filteredNewsLiveData

    // Filter state
    private val _selectedCategories = MutableLiveData<List<String>>(listOf("Business", "Technology"))
    val selectedCategories: LiveData<List<String>> get() = _selectedCategories

    private val _selectedSources = MutableLiveData<List<String>>(emptyList())
    val selectedSources: LiveData<List<String>> get() = _selectedSources

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

    // All available sources from API
    private val _availableSources = MutableLiveData<Set<String>>(emptySet())
    val availableSources: LiveData<Set<String>> get() = _availableSources

    fun fetchTopHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getTopHeadlines()
                if (response.isSuccessful && response.body() != null) {
                    val newsResponse = response.body()!!
                    _businessNews.value = newsResponse
                    businessFetched = true

                    // Extract sources from business news
                    extractSources(newsResponse.articles)

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
                    val newsResponse = response.body()!!
                    _techCrunchNews.value = newsResponse
                    techCrunchFetched = true

                    // Extract sources from tech news
                    extractSources(newsResponse.articles)

                    combineNewsIfReady()
                } else {
                    _errorMessage.value = "Failed to fetch TechCrunch news"
                }
            } catch (e: Exception) {
                _errorMessage.value = "TechCrunch Error: ${e.message}"
            }
        }
    }

    private fun extractSources(articles: List<Article>) {
        val currentSources = _availableSources.value?.toMutableSet() ?: mutableSetOf()
        articles.forEach { article ->
            article.source.name?.let { sourceName ->
                currentSources.add(sourceName)
            }
        }
        _availableSources.value = currentSources
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
            applyFilters()
            _isLoading.value = false
        }
    }

    // NEW: Apply filters function
    fun applyFilters(categories: List<String>? = null, sources: List<String>? = null) {
        // Update filter state if provided
        categories?.let { _selectedCategories.value = it }
        sources?.let { _selectedSources.value = it }

        val allArticles = _allNewsLiveData.value ?: emptyList()

        if (allArticles.isEmpty()) return

        var filtered = allArticles

        // Filter by category
        val selectedCats = _selectedCategories.value ?: emptyList()
        if (selectedCats.isNotEmpty()) {
            filtered = filtered.filter { article ->
                when {
                    article.source.name?.contains("TechCrunch", ignoreCase = true) == true ->
                        selectedCats.any { it.equals("Technology", ignoreCase = true) }
                    else -> selectedCats.any { it.equals("Business", ignoreCase = true) }
                }
            }
        }

        // Filter by source
        val selectedSrcs = _selectedSources.value ?: emptyList()
        if (selectedSrcs.isNotEmpty()) {
            filtered = filtered.filter { article ->
                selectedSrcs.any { source ->
                    article.source.name?.contains(source, ignoreCase = true) == true
                }
            }
        }

        _filteredNewsLiveData.value = filtered
    }

    // NEW: Clear filters
    fun clearFilters() {
        _selectedCategories.value = listOf("Business", "Technology")
        _selectedSources.value = emptyList()
        applyFilters()
    }

    // Search news function
    fun searchNews(query: String) {
        _searchQuery.value = query

        searchJob?.cancel()

        if (query.isEmpty()) {
            // When search is cleared, show filtered news again
            applyFilters()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)

            _isLoading.value = true
            try {
                val response = repository.searchNews(query)
                if (response.isSuccessful && response.body() != null) {
                    val searchResults = response.body()!!.articles

                    // Apply filters to search results too
                    var filtered = searchResults

                    // Filter by category
                    val selectedCats = _selectedCategories.value ?: emptyList()
                    if (selectedCats.isNotEmpty()) {
                        filtered = filtered.filter { article ->
                            when {
                                article.source.name?.contains("TechCrunch", ignoreCase = true) == true ->
                                    selectedCats.any { it.equals("Technology", ignoreCase = true) }
                                else -> selectedCats.any { it.equals("Business", ignoreCase = true) }
                            }
                        }
                    }

                    // Filter by source
                    val selectedSrcs = _selectedSources.value ?: emptyList()
                    if (selectedSrcs.isNotEmpty()) {
                        filtered = filtered.filter { article ->
                            selectedSrcs.any { source ->
                                article.source.name?.contains(source, ignoreCase = true) == true
                            }
                        }
                    }

                    _filteredNewsLiveData.value = filtered
                } else {
                    _errorMessage.value = "No results found for '$query'"
                    _filteredNewsLiveData.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search error: ${e.message}"
                _filteredNewsLiveData.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        applyFilters()
    }

    // Get current filter summary
    fun getFilterSummary(): String {
        val categories = _selectedCategories.value ?: emptyList()
        val sources = _selectedSources.value ?: emptyList()

        return when {
            categories.isEmpty() && sources.isEmpty() -> "All News"
            categories.size == 2 && sources.isEmpty() -> "All News"
            categories.size == 1 && sources.isEmpty() -> "${categories.first()} News"
            sources.isNotEmpty() && categories.isEmpty() -> "${sources.size} Sources"
            else -> "${categories.joinToString(", ")} (${sources.size} sources)"
        }
    }
}