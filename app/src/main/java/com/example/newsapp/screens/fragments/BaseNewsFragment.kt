package com.example.newsapp.screens.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newsapp.adapters.BookmarkAdapter
import com.example.newsapp.adapters.NewsPagingAdapter
import com.example.newsapp.data.models.Article
import com.example.newsapp.viewmodels.NewsViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

abstract class BaseNewsFragment : Fragment() {
    @Inject
    lateinit var dateFormatter: com.example.newsapp.utils.DateFormatter


    protected open val newsViewModel: NewsViewModel by activityViewModels()

    protected fun openArticleDetail(article: Article) {
        // Use SafeArgs dynamically
        val action = when (this) {
            is HomeFragment -> HomeFragmentDirections.actionHomeToArticleDetailFragment(article)
            is DiscoverFragment -> DiscoverFragmentDirections.actionDiscoverToArticleDetailFragment(
                article
            )

            is BookmarksFragment -> BookmarksFragmentDirections.actionBookmarkToArticleDetailFragment(
                article
            )

            else -> throw IllegalArgumentException("Unknown fragment type")
        }
        findNavController().navigate(action)
    }

    protected fun toggleBookmark(article: Article) {
        newsViewModel.toggleBookmark(article)
    }

    protected fun observeBookmarkUpdates(newsAdapter: Any) {
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.bookmarkStateChanged.collect { (url) ->
                when (newsAdapter) {
                    is NewsPagingAdapter -> newsAdapter.updateBookmarkIconForUrl(url)
                    is BookmarkAdapter -> {
                        val list = newsAdapter.currentList.toMutableList()
                        list.forEachIndexed { index, article ->
                            if (article.url == url) newsAdapter.notifyItemChanged(index)
                        }
                    }
                }
            }
        }
    }
}
