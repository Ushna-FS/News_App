package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.repository.BookmarkRepository
import com.example.newsapp.data.models.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
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
        updateBookmarkStatus()
        _isLoading.value = false
    }

    private fun updateBookmarkStatus() {
        viewModelScope.launch {
            _article.value?.let { article ->
                _isBookmarked.value = bookmarkRepository.isBookmarked(article.url)
            }
        }
    }
}