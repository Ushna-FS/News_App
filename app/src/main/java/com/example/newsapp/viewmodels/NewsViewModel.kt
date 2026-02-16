package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.newsapp.R
import com.example.newsapp.data.repository.BookmarkRepository
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.data.repository.SortType
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.models.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository, private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {
    // For HomeFragment - Only Business news
    val businessNewsPagingData: Flow<PagingData<Article>> =
        repository.getBusinessNewsStream().cachedIn(viewModelScope).flowOn(Dispatchers.IO)

    // StateFlows for filters and sort
    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())

    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    private val _selectedSources = MutableStateFlow<List<String>>(emptyList())
    val selectedSources: StateFlow<List<String>> = _selectedSources.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.NEWEST_FIRST)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _uiMessage = MutableSharedFlow<Int>()
    val uiMessage = _uiMessage.asSharedFlow()
    private val _bookmarkStateChanged = MutableSharedFlow<Pair<String, Boolean>>(replay = 1)
    val bookmarkStateChanged = _bookmarkStateChanged.asSharedFlow()

    init {
        viewModelScope.launch {
            bookmarkRepository.bookmarkUpdates.collect { url ->
                val isBookmarked = bookmarkRepository.isBookmarked(url)
                _bookmarkStateChanged.emit(Pair(url, isBookmarked))
                if (!isBookmarked) {
                    // show toast when removed from BookmarksFragment
                    _uiMessage.emit(R.string.bookmark_removed)
                }
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val newsPagingData: Flow<PagingData<Article>> = searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                combinedNewsPagingData
            } else {
                searchNewsPagingData
            }
        }
        .flowOn(Dispatchers.IO)


    @OptIn(ExperimentalCoroutinesApi::class)
    val combinedNewsPagingData: Flow<PagingData<Article>> = combine(
        selectedCategories, selectedSources, sortType
    ) { categories, sources, sortType ->
        Triple(categories, sources, sortType)
    }.distinctUntilChanged() // Only emit when something changes
        .flatMapLatest { (categories, sources, sortType) ->
            repository.getCombinedNewsStream(
                categories = categories, sources = sources, sortType = sortType
            ).cachedIn(viewModelScope)
        }.flowOn(Dispatchers.IO)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchNewsPagingData: Flow<PagingData<Article>> =
        _searchQuery.debounce(300).distinctUntilChanged().flatMapLatest { query ->
            if (query.isBlank()) {
                emptyFlow()
            } else {
                repository.searchNewsStream(query).cachedIn(viewModelScope)
            }
        }.flowOn(Dispatchers.IO)

    private val _hasUserSelectedSort = MutableStateFlow(false)
    val hasUserSelectedSort: StateFlow<Boolean> = _hasUserSelectedSort.asStateFlow()

    private val _availableSources = MutableStateFlow<Set<String>>(emptySet())
    val availableSources: StateFlow<Set<String>> = _availableSources.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Add active filters state for DiscoverFragment
    private val _activeFilters = MutableStateFlow<Triple<List<String>, List<String>, Boolean>>(
        Triple(emptyList(), emptyList(), false)
    )
    val activeFilters: StateFlow<Triple<List<String>, List<String>, Boolean>> =
        _activeFilters.asStateFlow()

    private fun updateActiveFilters() {
        val hasCustomCategories = _selectedCategories.value.isNotEmpty()
        val hasCustomSources = _selectedSources.value.isNotEmpty()
        _activeFilters.value = Triple(
            if (hasCustomCategories) _selectedCategories.value else emptyList(),
            if (hasCustomSources) _selectedSources.value else emptyList(),
            hasCustomCategories || hasCustomSources
        )
    }


    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun searchNews(query: String) {
        _searchQuery.value = query
    }

    fun extractSourceFromArticle(article: Article) {
        val currentSources = _availableSources.value.toMutableSet()
        article.source.name.let { sourceName ->
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

    fun toggleSource(source: String, isChecked: Boolean) {
        val current = _selectedSources.value.toMutableList()

        if (isChecked) current.add(source)
        else current.remove(source)

        _selectedSources.value = current.distinct()
        updateActiveFilters()
    }

    fun applyFilters(categories: List<String>, sources: List<String>) {
        _selectedCategories.value = categories
        _selectedSources.value = sources
        updateActiveFilters()
    }

    fun getFilterSummaryData(): Triple<List<String>, List<String>, Boolean> {
        val categories = _selectedCategories.value
        val sources = _selectedSources.value
        val hasAny = categories.isNotEmpty() || sources.isNotEmpty()
        return Triple(categories, sources, hasAny)
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            val url = article.url
            val isBookmarked = bookmarkRepository.isBookmarked(url)

            if (isBookmarked) {
                bookmarkRepository.removeBookmark(article) // This will emit
                _uiMessage.emit(R.string.bookmark_removed)
            } else {
                bookmarkRepository.addBookmark(article) // This will emit
                _uiMessage.emit(R.string.bookmark_added)
            }

        }
    }

    fun getAllBookmarkedUrls(): Flow<Set<String>> = flow {
        bookmarkRepository.getAllBookmarks().collect { bookmarks ->
            emit(bookmarks.map { it.url }.toSet())
        }
    }.flowOn(Dispatchers.IO)

    fun isArticleBookmarked(url: String): Flow<Boolean> = flow {
        val isBookmarked = bookmarkRepository.isBookmarked(url)
        emit(isBookmarked)
    }.flowOn(Dispatchers.IO)

    val bookmarks: Flow<List<BookmarkedArticle>> =
        bookmarkRepository.getAllBookmarks().flowOn(Dispatchers.IO)

    fun hasActiveFilters(): Boolean {
        val hasCustomCategories = _selectedCategories.value.isNotEmpty()
        val hasCustomSources = _selectedSources.value.isNotEmpty()
        return hasCustomCategories || hasCustomSources
    }


}