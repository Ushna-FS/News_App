package com.example.newsapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.newsapp.data.Repository.BookmarkRepository
import com.example.newsapp.data.Repository.NewsRepository
import com.example.newsapp.data.Repository.SortType
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.models.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {
    // For HomeFragment - Only Business news
    val businessNewsPagingData: Flow<PagingData<Article>> =
        repository.getBusinessNewsStream()
            .cachedIn(viewModelScope)
            .flowOn(Dispatchers.IO)

    // StateFlows for filters and sort
    private val _selectedCategories = MutableStateFlow(listOf("Business", "Technology"))
    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    private val _selectedSources = MutableStateFlow<List<String>>(emptyList())
    val selectedSources: StateFlow<List<String>> = _selectedSources.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.NEWEST_FIRST)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _bookmarkStateChanged = MutableStateFlow<Pair<String, Boolean>?>(null)
    val bookmarkStateChanged: StateFlow<Pair<String, Boolean>?> =
        _bookmarkStateChanged.asStateFlow()

    init {
        viewModelScope.launch {
            bookmarkRepository.bookmarkUpdates.collect { url ->
                // When bookmark changes, update the state
                val isBookmarked = bookmarkRepository.isBookmarked(url)
                _bookmarkStateChanged.value = Pair(url, isBookmarked)
            }
        }
    }

    // UPDATED: Combine all filter states and pass to repository
    val combinedNewsPagingData: Flow<PagingData<Article>> = combine(
        selectedCategories,
        selectedSources,
        sortType
    ) { categories, sources, sortType ->
        Triple(categories, sources, sortType)
    }
        .distinctUntilChanged() // Only emit when something changes
        .flatMapLatest { (categories, sources, sortType) ->
            repository.getCombinedNewsStream(
                categories = categories,
                sources = sources,
                sortType = sortType
            )
                .cachedIn(viewModelScope)
        }
        .flowOn(Dispatchers.IO)

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchNewsPagingData: Flow<PagingData<Article>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                emptyFlow()
            } else {
                repository.searchNewsStream(query)
                    .cachedIn(viewModelScope)
            }
        }
        .flowOn(Dispatchers.IO)

    private val _hasUserSelectedSort = MutableStateFlow(false)
    val hasUserSelectedSort: StateFlow<Boolean> = _hasUserSelectedSort.asStateFlow()

    private val _availableSources = MutableStateFlow<Set<String>>(emptySet())
    val availableSources: StateFlow<Set<String>> = _availableSources.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()


    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun searchNews(query: String) {
        _searchQuery.value = query
    }

    fun extractSourceFromArticle(article: Article) {
        val currentSources = _availableSources.value.toMutableSet()
        article.source?.name?.let { sourceName ->
            if (sourceName.isNotEmpty()) {
                currentSources.add(sourceName)
            }
        }
        _availableSources.value = currentSources
    }

    fun setSortType(sortType: SortType) {
        _hasUserSelectedSort.value = true
        _sortType.value = sortType
    }

    fun resetSort() {
        _hasUserSelectedSort.value = false
        _sortType.value = SortType.NEWEST_FIRST
    }

    fun applyFilters(categories: List<String>, sources: List<String>) {
        _selectedCategories.value = categories
        _selectedSources.value = sources
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

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            val url = article.url ?: return@launch
            val isBookmarked = bookmarkRepository.isBookmarked(url)

            if (isBookmarked) {
                bookmarkRepository.removeBookmark(article)
                // Show message immediately
                _bookmarkStateChanged.value = Pair(url, false)
            } else {
                bookmarkRepository.addBookmark(article)
                // Show message immediately
                _bookmarkStateChanged.value = Pair(url, true)
            }
        }
    }

    fun isArticleBookmarked(url: String): Flow<Boolean> = flow {
        val isBookmarked = bookmarkRepository.isBookmarked(url)
        emit(isBookmarked)
    }.flowOn(Dispatchers.IO)

    val bookmarks: Flow<List<BookmarkedArticle>> = bookmarkRepository.getAllBookmarks()
        .flowOn(Dispatchers.IO)
}