package com.example.newsapp.ViewModels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.Data.models.NewsResponse
import com.example.newsapp.Data.Repository.NewsRepository
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val repository = NewsRepository()

    // LiveData to observe news data
    private val _newsLiveData = MutableLiveData<NewsResponse>()
    val newsLiveData: LiveData<NewsResponse> get() = _newsLiveData

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchTopHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getTopHeadlines()
                if (response.isSuccessful && response.body() != null) {
                    _newsLiveData.value = response.body()!!
                } else {
                    _errorMessage.value = "Failed to fetch news: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}