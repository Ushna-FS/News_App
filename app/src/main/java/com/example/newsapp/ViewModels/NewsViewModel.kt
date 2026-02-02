package com.example.newsapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.Data.models.Article
import com.example.newsapp.Data.Repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    // Current page state
    private val _currentPage = MutableStateFlow(1)
    private val _pageSize = MutableStateFlow(5)
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    // Track loading more separately
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Paginated data
    private val _paginatedNews = MutableStateFlow<List<Article>>(emptyList())

    // Rest of your existing code...
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
    private val _businessNews = MutableStateFlow<List<Article>>(emptyList()) // Changed from NewsResponse
    private val _techCrunchNews = MutableStateFlow<List<Article>>(emptyList()) // Changed from NewsResponse
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
            .distinctUntilChanged()
            .filter { query -> query.isNotEmpty() }
            .onEach { query ->
                // Reset page when searching new query
                _currentPage.value = 1
                _paginatedNews.value = emptyList()
                performSearch(query, 1)
            }
            .launchIn(viewModelScope)
    }



    fun loadMoreNews() {
        if (_isLoadingMore.value || !_hasMorePages.value || _isLoading.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true

            // ✅ 3-4 second loading delay
            delay(3500L)

            val nextPage = _currentPage.value + 1

            try {
                when {
                    _searchQuery.value.isNotEmpty() -> {
                        loadMoreSearchResults(nextPage)
                    }
                    else -> {
                        loadMoreHeadlines(nextPage)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading more articles: ${e.message}"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private suspend fun loadMoreHeadlines(page: Int) {
        // Business news
        val businessResponse = repository.getTopHeadlines(
            page = page,
            pageSize = _pageSize.value  // ✅ Use pageSize
        )
        if (businessResponse.isSuccessful) {
            businessResponse.body()?.let { newsResponse ->
                val newArticles = newsResponse.articles
                // ✅ Prevent duplicates
                val existingUrls = _businessNews.value.map { it.url }.toSet()
                val uniqueNewArticles = newArticles.filterNot { existingUrls.contains(it.url) }

                _businessNews.value = _businessNews.value + uniqueNewArticles
                // ✅ Better hasMorePages logic
                _hasMorePages.value = uniqueNewArticles.size >= _pageSize.value
                _currentPage.value = page
                extractSources(uniqueNewArticles)
            }
        }
        val techResponse = repository.getTechCrunchHeadlines(
            page = page,
            pageSize = _pageSize.value  // ✅ Use pageSize
        )
        if (techResponse.isSuccessful) {
            techResponse.body()?.let { newsResponse ->
                val newArticles = newsResponse.articles
                val existingUrls = _techCrunchNews.value.map { it.url }.toSet()
                val uniqueNewArticles = newArticles.filterNot { existingUrls.contains(it.url) }

                _techCrunchNews.value = _techCrunchNews.value + uniqueNewArticles
                _hasMorePages.value = uniqueNewArticles.size >= _pageSize.value
                extractSources(uniqueNewArticles)
            }
        }

        combineNewsIfReady()
    }
    private suspend fun loadMoreSearchResults(page: Int) {
        val response = repository.searchNews(
            _searchQuery.value,
            page = page,
            pageSize = _pageSize.value  // ✅ Use pageSize
        )
        if (response.isSuccessful) {
            response.body()?.let { searchResults ->
                val newArticles = searchResults.articles
                val existingUrls = _paginatedNews.value.map { it.url }.toSet()
                val uniqueNewArticles = newArticles.filterNot { existingUrls.contains(it.url) }

                _paginatedNews.value = _paginatedNews.value + uniqueNewArticles
                _hasMorePages.value = uniqueNewArticles.size >= _pageSize.value
                _currentPage.value = page

                val filtered = applyFiltersInternal(
                    _paginatedNews.value,
                    _selectedCategories.value,
                    _selectedSources.value,
                    _sortType.value
                )
                _filteredNews.value = filtered
            }
        }
    }
    private suspend fun performSearch(query: String, page: Int = 1) {
        _isLoading.value = true
        try {
            val response = repository.searchNews(query, page = page)
            if (response.isSuccessful) {
                response.body()?.let { searchResults ->
                    _paginatedNews.value = searchResults.articles
                    _hasMorePages.value = searchResults.articles.size >= _pageSize.value
                    _currentPage.value = page

                    // Apply current filters to search results
                    val filtered = applyFiltersInternal(
                        searchResults.articles,
                        _selectedCategories.value,
                        _selectedSources.value,
                        _sortType.value
                    )
                    _filteredNews.value = filtered
                    _errorMessage.value = ""
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

    fun fetchTopHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPage.value = 1
            _businessNews.value = emptyList()
            _techCrunchNews.value = emptyList()

            try {
                val businessResponse = repository.getTopHeadlines(page = 1)
                if (businessResponse.isSuccessful) {
                    businessResponse.body()?.let { newsResponse ->
                        _businessNews.value = newsResponse.articles
                        extractSources(newsResponse.articles)
                    }
                }

                val techResponse = repository.getTechCrunchHeadlines(page = 1)
                if (techResponse.isSuccessful) {
                    techResponse.body()?.let { newsResponse ->
                        _techCrunchNews.value = newsResponse.articles
                        extractSources(newsResponse.articles)
                    }
                }

                combineNewsIfReady()
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTechCrunchHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getTechCrunchHeadlines(page = _currentPage.value)
                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        val newArticles = _techCrunchNews.value + newsResponse.articles
                        _techCrunchNews.value = newArticles
                        extractSources(newsResponse.articles)
                        combineNewsIfReady()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "TechCrunch Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    private fun combineNewsIfReady() {
        val businessArticles = _businessNews.value
        val techArticles = _techCrunchNews.value

        // Combine all articles
        val combined = (businessArticles + techArticles)

        if (combined.isNotEmpty()) {
            val sorted = combined.sortedByDescending { it.publishedAt }
            _allNews.value = sorted
        }
    }

    // Rest of your existing methods remain the same...
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

    fun searchNews(query: String) {
        // Update UI state immediately
        _searchQuery.value = query

        // Emit to SharedFlow for debounced processing
        viewModelScope.launch {
            _searchEvents.emit(query)
        }

        // If query is empty, clear search results immediately
        if (query.isEmpty()) {
            clearSearch()
        }
    }
    fun clearSearch() {
        _searchQuery.value = ""
        _paginatedNews.value = emptyList()
        _currentPage.value = 1
        clearSearchResults()
    }

    private fun clearSearchResults() {
        _filteredNews.value = applyFiltersInternal(
            _allNews.value,
            _selectedCategories.value,
            _selectedSources.value,
            _sortType.value
        )
        _errorMessage.value = ""
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