package com.example.newsapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.Data.models.Article
import com.example.newsapp.Data.models.NewsResponse
import com.example.newsapp.Data.Repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    // MutableStateFlow for UI STATE
    private val _filteredNews = MutableStateFlow<List<Article>>(emptyList())
    val filteredNews: StateFlow<List<Article>> = _filteredNews.asStateFlow()

    private val _selectedCategories = MutableStateFlow(listOf("Business", "Technology"))
    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    private val _selectedSources = MutableStateFlow<List<String>>(emptyList())
    val selectedSources: StateFlow<List<String>> = _selectedSources.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _availableSources = MutableStateFlow<Set<String>>(emptySet())
    val availableSources: StateFlow<Set<String>> = _availableSources.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.NEWEST_FIRST)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _hasUserSelectedSort = MutableStateFlow(false)
    val hasUserSelectedSort: StateFlow<Boolean> = _hasUserSelectedSort.asStateFlow()

    // MutableSharedFlow for SEARCH EVENTS
    private val _searchEvents = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64
    )

    // Private mutable data
    private val _businessNews = MutableStateFlow<NewsResponse?>(null)
    private val _techCrunchNews = MutableStateFlow<NewsResponse?>(null)
    private val _allNews = MutableStateFlow<List<Article>>(emptyList())

    init {
        // When any filter changes, auto-apply filters
        combine(
            _allNews,
            _selectedCategories,
            _selectedSources,
            _sortType
        ) { allNews, categories, sources, sortType ->
            applyFiltersInternal(allNews, categories, sources, sortType)
        }.onEach { filtered ->
            _filteredNews.value = filtered
        }.launchIn(viewModelScope)

        setupSearchPipeline()
    }

    enum class SortType { NEWEST_FIRST, OLDEST_FIRST }

    private fun setupSearchPipeline() {
        _searchEvents
            .debounce(400)
            .distinctUntilChanged() // Only emit if query changed
            .filter { query -> query.isNotEmpty() } // Skip empty queries
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }

    //search function
    fun searchNews(query: String) {
        // Update UI state immediately
        _searchQuery.value = query

        // Emit to SharedFlow for debounced processing
        viewModelScope.launch {
            _searchEvents.emit(query)
        }

        // If query is empty, clear search results immediately
        if (query.isEmpty()) {
            clearSearchResults()
        }
    }

    //PERFORM ACTUAL SEARCH
    private suspend fun performSearch(query: String) {
        _isLoading.value = true
        try {
            val response = repository.searchNews(query)
            if (response.isSuccessful) {
                response.body()?.let { searchResults ->
                    // Apply current filters to search results
                    val filtered = applyFiltersInternal(
                        searchResults.articles,
                        _selectedCategories.value,
                        _selectedSources.value,
                        _sortType.value
                    )
                    _filteredNews.value = filtered
                    _errorMessage.value = "" // Clear any previous errors
                }
            } else {
                _errorMessage.value = "No results found for '$query'"
                _filteredNews.value = emptyList()
            }
        } catch (e: Exception) {
            _errorMessage.value = "Search error: ${e.message}"
            _filteredNews.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    // CLEAR SEARCH RESULTS
    private fun clearSearchResults() {
        _filteredNews.value = applyFiltersInternal(
            _allNews.value,
            _selectedCategories.value,
            _selectedSources.value,
            _sortType.value
        )
        _errorMessage.value = ""
    }

    fun fetchTopHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getTopHeadlines()
                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        _businessNews.value = newsResponse
                        extractSources(newsResponse.articles)
                        combineNewsIfReady()
                    }
                } else {
                    _errorMessage.value = "Failed to fetch business news"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Business Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTechCrunchHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getTechCrunchHeadlines()
                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        _techCrunchNews.value = newsResponse
                        extractSources(newsResponse.articles)
                        combineNewsIfReady()
                    }
                } else {
                    _errorMessage.value = "Failed to fetch TechCrunch news"
                }
            } catch (e: Exception) {
                _errorMessage.value = "TechCrunch Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun combineNewsIfReady() {
        val businessArticles = _businessNews.value?.articles ?: emptyList()
        val techArticles = _techCrunchNews.value?.articles ?: emptyList()

        // Combine only if we have articles
        val combined = (businessArticles + techArticles)

        if (combined.isNotEmpty()) {
            val sorted = combined.sortedByDescending { it.publishedAt }
            _allNews.value = sorted
        }
    }

    private fun extractSources(articles: List<Article>) {
        val currentSources = _availableSources.value.toMutableSet()
        articles.forEach { article ->
            article.source?.name?.let { sourceName ->
                if (sourceName.isNotEmpty()) {
                    currentSources.add(sourceName)
                }
            }
        }
        _availableSources.value = currentSources
    }

    private fun applyFiltersInternal(
        allArticles: List<Article>,
        categories: List<String>,
        sources: List<String>,
        sortType: SortType
    ): List<Article> {
        var filtered = allArticles

        // Filter by category
        if (categories.isNotEmpty()) {
            filtered = filtered.filter { article ->
                when {
                    article.source.name.contains("TechCrunch", ignoreCase = true) ->
                        categories.any { it.equals("Technology", ignoreCase = true) }
                    else -> categories.any { it.equals("Business", ignoreCase = true) }
                }
            }
        }

        // Filter by source
        if (sources.isNotEmpty()) {
            filtered = filtered.filter { article ->
                sources.any { source ->
                    article.source.name.contains(source, ignoreCase = true)
                }
            }
        }

        // Apply sorting
        return when (sortType) {
            SortType.NEWEST_FIRST -> filtered.sortedByDescending { it.publishedAt }
            SortType.OLDEST_FIRST -> filtered.sortedBy { it.publishedAt }
        }
    }

    fun setSortType(sortType: SortType) {
        _hasUserSelectedSort.value = true
        _sortType.value = sortType
    }

    fun resetSort() {
        _hasUserSelectedSort.value = false
        _sortType.value = SortType.NEWEST_FIRST
    }

    fun clearSearch() {
        _searchQuery.value = ""
        clearSearchResults()
    }

    fun getFilterSummary(): String {
        val categories = _selectedCategories.value
        val sources = _selectedSources.value

        return when {
            categories.isEmpty() && sources.isEmpty() -> "All News"
            categories.size == 2 && sources.isEmpty() -> "All News"
            categories.size == 1 && sources.isEmpty() -> "${categories.first()} News"
            sources.isNotEmpty() && categories.isEmpty() -> "${sources.size} Sources"
            else -> "${categories.joinToString(", ")} (${sources.size} sources)"
        }
    }

    fun applyFilters(categories: List<String>, sources: List<String>) {
        _selectedCategories.value = categories
        _selectedSources.value = sources
    }
}