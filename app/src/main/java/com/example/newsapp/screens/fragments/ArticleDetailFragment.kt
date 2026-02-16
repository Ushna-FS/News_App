package com.example.newsapp.screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.newsapp.R
import com.example.newsapp.data.models.Article
import com.example.newsapp.viewmodels.ArticleDetailViewModel
import com.example.newsapp.databinding.FragmentArticleDetailBinding
import com.example.newsapp.viewmodels.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebChromeClient
import kotlinx.coroutines.delay


@AndroidEntryPoint
class ArticleDetailFragment : Fragment() {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticleDetailViewModel by viewModels()
    private val newsViewModel: NewsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupWebView()
        observeViewModelStates()
        if (viewModel.article.value == null) {
            loadArticle()
        }

    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_bookmark) {
                viewModel.article.value?.let {
                    newsViewModel.toggleBookmark(it)
                }
                true
            } else false
        }
    }

    private fun setupWebView() {

        binding.webView.isVisible = false
        binding.progressBar.isVisible = true
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, progress: Int) {
                val b = _binding ?: return   // ← SAFE EXIT

                if (progress > 60) {
                    b.progressBar.isVisible = false
                    b.webView.isVisible = true
                }
            }
        }

        val ws = binding.webView.settings

        ws.javaScriptEnabled = true
        ws.domStorageEnabled = true
        ws.loadsImagesAutomatically = true
        ws.blockNetworkImage = false
        ws.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        ws.setSupportZoom(false)
        ws.mediaPlaybackRequiresUserGesture = true
        ws.databaseEnabled = true
    }

    private fun observeViewModelStates() {
        // Check bookmark state when article loads
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.article.collectLatest { article ->
                article?.let {
                    displayArticle(it)

                    // Check initial bookmark state
                    newsViewModel.isArticleBookmarked(it.url).collectLatest { isBookmarked ->
                        updateBookmarkIcon(isBookmarked)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Listen for ALL bookmark changes
            newsViewModel.bookmarkStateChanged.collect { (url, isBookmarked) ->
                viewModel.article.value?.let { article ->
                    if (article.url == url) {
                        updateBookmarkIcon(isBookmarked)

                    }
                }
            }
        }
    }

    private fun loadArticle() {
        val article = arguments?.getSerializable(ARG_ARTICLE) as? Article
        if (article != null) {
            viewModel.setArticle(article)
        } else {
            showError()
        }
    }

    private fun displayArticle(article: Article) {
        binding.toolbar.title = article.title
        binding.webView.loadUrl(article.url)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(8000)
            _binding?.let {
                it.progressBar.isVisible = false
                it.webView.isVisible = true
            }
        }
    }

    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        val icon = if (isBookmarked)
            R.drawable.ic_bookmark
        else
            R.drawable.ic_bookmark_border

        binding.toolbar.menu
            .findItem(R.id.action_bookmark)
            ?.setIcon(icon)
    }

    private fun showError() {
        val message = getString(R.string.no_article_data)
        binding.progressBar.isVisible = false
        binding.tvError.isVisible = true
        binding.tvError.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        binding.webView.webChromeClient = null
        binding.webView.stopLoading()
        binding.webView.destroy()
        _binding = null
        super.onDestroyView()
    }

    //
    companion object {
        private const val ARG_ARTICLE = "arg_article"

    }
}