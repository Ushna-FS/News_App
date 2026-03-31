package com.example.newsapp.screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.newsapp.adapters.BookmarkAdapter
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.models.Source
import com.example.newsapp.databinding.FragmentBookmarksBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarksFragment : BaseNewsFragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    override val newsViewModel: NewsViewModel by activityViewModels()
    private lateinit var bookmarkAdapter: BookmarkAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        observeBookmarkUpdates(bookmarkAdapter)
    }


    private fun setupRecyclerView() {
        bookmarkAdapter = BookmarkAdapter(
            onReadMoreClick = { bookmarked ->
                // convert to Article only when opening detail
                openArticleDetail(bookmarked.toArticle())
            },
            toggleBookmark = { bookmarked ->
                toggleBookmark(bookmarked.toArticle())
            },
            onShareClick = { bookmarked ->
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, bookmarked.url)
                    type = "text/plain"
                }
                startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
            },
            dateFormatter = dateFormatter
        )
        binding.recyclerViewBookmarks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarkAdapter
            setHasFixedSize(true)
        }
    }
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.bookmarks.collect { bookmarks ->
                bookmarkAdapter.submitList(bookmarks)

                // Show/hide empty state and RecyclerView
                val hasBookmarks = bookmarks.isNotEmpty()
                binding.textEmpty.isVisible = !hasBookmarks
                binding.recyclerViewBookmarks.isVisible = hasBookmarks
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.uiMessage.collect { resId ->
                Toast.makeText(
                    requireContext(), getString(resId), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Extension function to convert BookmarkedArticle to Article
private fun BookmarkedArticle.toArticle(): Article {
    return Article(
        source = Source(id = null, name = this.sourceName),
        author = this.author,
        title = this.title,
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content
    )
}