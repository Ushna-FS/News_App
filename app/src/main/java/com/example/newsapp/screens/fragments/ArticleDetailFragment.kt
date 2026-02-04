package com.example.newsapp.screens.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.ViewModels.ArticleDetailViewModel
import com.example.newsapp.data.models.getFormattedDate
import com.example.newsapp.data.models.getFullContent
import com.example.newsapp.databinding.FragmentArticleDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class ArticleDetailFragment : Fragment() {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticleDetailViewModel by viewModels()
    private val json = Json { ignoreUnknownKeys = true }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        if (viewModel.article.value == null) {
            loadArticle()
        }
    }

    private fun setupUI() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }

            ivBookmark.setOnClickListener {
                viewModel.toggleBookmark()
            }

            btnOpenInBrowser.setOnClickListener {
                viewModel.article.value?.url?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }

            btnShare.setOnClickListener {
                viewModel.article.value?.let { article ->
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.url}")
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share Article"))
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.article.collectLatest { article ->
                article?.let { displayArticle(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isBookmarked.collectLatest { isBookmarked ->
                updateBookmarkIcon(isBookmarked)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.isVisible = isLoading
                binding.scrollView.isVisible = !isLoading
            }
        }
    }

    private fun loadArticle() {
        arguments?.getString(ARG_ARTICLE_JSON)?.let { jsonString ->
            try {
                val article = json.decodeFromString<com.example.newsapp.data.models.Article>(jsonString)
                viewModel.setArticle(article)
            } catch (e: Exception) {
                showError("Failed to load article")
            }
        } ?: showError("No article data")
    }

    private fun displayArticle(article: com.example.newsapp.data.models.Article) {
        binding.apply {
            tvTitle.text = article.title ?: ""
            tvContent.text = article.getFullContent() // Now this will work
            tvAuthor.text = article.author ?: "Unknown Author"
            tvSource.text = article.source?.name ?: "Unknown Source"
            tvPublishedAt.text = article.getFormattedDate() // Now this will work

            article.urlToImage?.let { imageUrl ->
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_newspaper)
                    .into(ivArticleImage)
            } ?: run {
                ivArticleImage.setImageResource(R.drawable.ic_newspaper)
            }
        }
    }

    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        binding.ivBookmark.apply {
            setImageResource(
                if (isBookmarked) R.drawable.ic_bookmark
                else R.drawable.ic_bookmark_border
            )
            setColorFilter(requireContext().getColor(R.color.blueMain))
        }
    }

    private fun showError(message: String) {
        binding.progressBar.isVisible = false
        binding.tvError.isVisible = true
        binding.tvError.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ARTICLE_JSON = "article_json"

        fun newInstance(article: com.example.newsapp.data.models.Article): ArticleDetailFragment {
            val json = Json { ignoreUnknownKeys = true }
            return ArticleDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ARTICLE_JSON, json.encodeToString(article))
                }
            }
        }
    }
}