package com.example.newsapp.screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.adapters.BookmarkAdapter
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.local.toArticle
import com.example.newsapp.databinding.FragmentBookmarksBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private val newsViewModel: NewsViewModel by activityViewModels()
    private lateinit var bookmarkAdapter: BookmarkAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // No need to set text here - it's already set in XML
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        bookmarkAdapter = BookmarkAdapter(
            onItemClick = { article ->
                // Handle article click - open article detail
                // You can implement this later
            },
            onBookmarkClick = { article ->
                // Remove bookmark when clicked (since it's already bookmarked)
                newsViewModel.toggleBookmark(article)
            }
        )

        // Set up the RecyclerView
        binding.recyclerViewBookmarks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarkAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            newsViewModel.bookmarks.collect { bookmarks ->
                // Convert BookmarkedArticle to Article for the adapter
                val bookmarkedArticles = bookmarks.map { it.toArticle() }
                bookmarkAdapter.submitList(bookmarkedArticles)

                // Show/hide empty state and RecyclerView
                val hasBookmarks = bookmarks.isNotEmpty()
                binding.textEmpty.isVisible = !hasBookmarks
                binding.recyclerViewBookmarks.isVisible = hasBookmarks
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}