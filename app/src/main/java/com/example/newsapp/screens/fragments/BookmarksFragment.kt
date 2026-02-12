package com.example.newsapp.screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.newsapp.adapters.BookmarkAdapter
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.models.Source
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        bookmarkAdapter = BookmarkAdapter(onItemClick = { article ->
            // Open ArticleDetailFragment
            openArticleDetail(article)
        }, onBookmarkClick = { article ->
            // Remove bookmark when clicked (since it's already bookmarked)
            newsViewModel.toggleBookmark(article)
        })

        binding.recyclerViewBookmarks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarkAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
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
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.uiMessage.collect {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openArticleDetail(article: Article) {
        val fragmentManager = requireActivity().supportFragmentManager

        // Check if already showing to prevent duplicates
        if (fragmentManager.findFragmentByTag("ArticleDetail") != null) return

        fragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_left,
            R.anim.slide_out_right,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        ).replace(
            R.id.fragment_container, ArticleDetailFragment.newInstance(article), "ArticleDetail"
        ).addToBackStack("ArticleDetail").commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Extension function to convert BookmarkedArticle to Article
private fun BookmarkedArticle.toArticle():Article {
    return Article(
        source = Source(id = null, name = this.sourceName),
        author = this.author,
        title = this.title.toString(),
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content,
        isBookmarked = true
    )
}