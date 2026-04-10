package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.shared.data.local.BookmarkedArticle
import com.example.shared.data.models.Article
import com.example.shared.data.models.getCategory
import com.example.shared.data.repository.BookmarkRepository
import com.example.shared.data.repository.NewsRepository
import com.example.shared.data.repository.SortType
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.sample.library.resources.Res
import me.sample.library.resources.bookmark_added
import me.sample.library.resources.bookmark_removed
import org.jetbrains.compose.resources.StringResource


class NewsViewModel(
    private val repository: NewsRepository, private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {

    private val selectedHomeCategory = MutableStateFlow("All")
    private val categoryCache = mutableMapOf<String, Flow<PagingData<Article>>>()
    private val _homeCategoryLoader = MutableStateFlow(false)
    val homeCategoryLoader: StateFlow<Boolean> = _homeCategoryLoader


    // StateFlows for filters and sort
    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())

    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    private val _selectedSources = MutableStateFlow<List<String>>(emptyList())
    val selectedSources: StateFlow<List<String>> = _selectedSources.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.NEWEST_FIRST)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _uiMessage = MutableSharedFlow<StringResource>()
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
                    _uiMessage.emit(Res.string.bookmark_removed)
                }
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeNewsPagingData: Flow<PagingData<Article>> =
        selectedHomeCategory
            .flatMapLatest { category ->

                repository.getAllNewsStream().map { pagingData ->
                    if (category == "All") pagingData
                    else pagingData.filter {
                        it.getCategory().displayName == category
                    }

                }
            }
            .cachedIn(viewModelScope)

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
            )
        }
        .cachedIn(viewModelScope)
        .flowOn(Dispatchers.IO)

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

    private var currentUserId: String? = null

    fun setCurrentUser(userId: String) {
        currentUserId = userId
    }

    private fun updateActiveFilters() {
        val hasCustomCategories = _selectedCategories.value.isNotEmpty()
        val hasCustomSources = _selectedSources.value.isNotEmpty()
        _activeFilters.value = Triple(
            if (hasCustomCategories) _selectedCategories.value else emptyList(),
            if (hasCustomSources) _selectedSources.value else emptyList(),
            hasCustomCategories || hasCustomSources
        )
    }

    fun setHomeCategory(category: String) {
        viewModelScope.launch {
            _homeCategoryLoader.value = true  // show loader

            // Clear previous PagingData
            selectedHomeCategory.value = ""

            // Fetch only if not cached
            if (!categoryCache.containsKey(category)) {
                val flow = repository.getAllNewsStream()
                    .map { pagingData ->
                        if (category == "All") pagingData
                        else pagingData.filter { it.getCategory().displayName == category }
                    }
                    .cachedIn(viewModelScope)
                categoryCache[category] = flow
            }

            // Now set the actual category → triggers new data
            selectedHomeCategory.value = category
            _homeCategoryLoader.value = false
        }
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

        val userId = currentUserId ?: Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {

            val isBookmarked = bookmarkRepository.isBookmarked(article.url)

            if (isBookmarked) {
                bookmarkRepository.removeBookmark(article.url, userId)
                _uiMessage.emit(Res.string.bookmark_removed)
            } else {
                bookmarkRepository.addBookmark(article, userId)
                _uiMessage.emit(Res.string.bookmark_added)
            }
        }
    }

    fun getAllBookmarkedUrls(): Flow<Set<String>> = flow {
        bookmarkRepository.getAllBookmarks().collect { bookmarks ->
            emit(bookmarks.map { it.url }.toSet())
        }
    }.flowOn(Dispatchers.IO)

    fun isArticleBookmarked(url: String): Flow<Boolean> {
        return bookmarkRepository.getAllBookmarks()
            .map { bookmarks ->
                bookmarks.any { it.url == url }
            }
            .distinctUntilChanged()
    }

    val bookmarks: Flow<List<BookmarkedArticle>> =
        bookmarkRepository.getAllBookmarks().flowOn(Dispatchers.IO)

    fun hasActiveFilters(): Boolean {
        val hasCustomCategories = _selectedCategories.value.isNotEmpty()
        val hasCustomSources = _selectedSources.value.isNotEmpty()
        return hasCustomCategories || hasCustomSources
    }

    fun startBookmarkSync(userId: String) {
        bookmarkRepository.startRealtimeSync(userId)
    }
}