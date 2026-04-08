package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.repository.BookmarkRepository
import com.example.shared.data.models.Article
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ArticleDetailViewModel(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)

    init {
        // Listen for bookmark updates
        viewModelScope.launch {
            bookmarkRepository.bookmarkUpdates.collect { url ->
                _article.value?.let { article ->
                    if (article.url == url) {
                        updateBookmarkStatus()
                    }
                }
            }
        }
    }

    fun setArticle(article: Article) {
        _isLoading.value = true
        _article.value = article
        viewModelScope.launch {
            updateBookmarkStatus()
            _isLoading.value = false
        }
    }

    private suspend fun updateBookmarkStatus() {
        _article.value?.let { article ->
            _isBookmarked.value = bookmarkRepository.isBookmarked(article.url)
        }
    }
}