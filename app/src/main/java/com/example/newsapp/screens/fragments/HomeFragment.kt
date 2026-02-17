package com.example.newsapp.screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.MainActivity
import com.example.newsapp.R
import com.example.newsapp.adapters.CardType
import com.example.newsapp.adapters.NewsPagingAdapter
import com.example.newsapp.databinding.FragmentHomeBinding
import com.example.newsapp.viewmodels.NewsViewModel
import com.example.newsapp.data.models.Article
import com.example.newsapp.utils.DateFormatter
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val newsViewModel: NewsViewModel by activityViewModels()
    private lateinit var newsAdapter: NewsPagingAdapter

    @Inject
    lateinit var dateFormatter: DateFormatter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = binding.includeToolbar.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.includeToolbar.toolbar)
        toolbar.title = "NewsMate"
        setupRecyclerView()
        setupObservers()
        setupRetryButton()
        observeBookmarkUpdates()
        binding.textWelcome.text = getString(R.string.home_user)
        toolbar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
    }

    private fun observeBookmarkUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.bookmarkStateChanged.collect { (url) ->
                    newsAdapter.updateBookmarkIconForUrl(url)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsPagingAdapter(
            cardType = CardType.HOME, onItemClick = { article ->
            openArticleDetail(article)
        }, onBookmarkClick = { article ->
            newsViewModel.toggleBookmark(article)
        }, onExtractSource = { article ->
            newsViewModel.extractSourceFromArticle(article)
        }, dateFormatter = dateFormatter
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter.withLoadStateFooter(
                footer = NewsPagingAdapter.NewsLoadStateAdapter { newsAdapter.retry() })
            setHasFixedSize(true)
        }

        // Set up load state listener
        newsAdapter.addLoadStateListener { loadState ->
            val refresh = loadState.refresh
            val isListEmpty = newsAdapter.itemCount == 0

            // ---------------- INITIAL LOAD ERROR ----------------
            if (refresh is LoadState.Error) {
                binding.progressBar.isVisible = false
                binding.recyclerView.isVisible = false
                binding.llEmptyState.isVisible = true

                // Show retry button for initial load errors
                binding.btnRetry.isVisible = true

                val error = refresh.error
                binding.textEmpty.text = when (error) {
                    is IOException, is SocketTimeoutException -> getString(R.string.error_check_internet)

                    else -> getString(R.string.error_load_articles)
                }
                return@addLoadStateListener
            }

            // ---------------- INITIAL LOADING ----------------
            if (refresh is LoadState.Loading) {
                binding.progressBar.isVisible = true
                binding.recyclerView.isVisible = false
                binding.llEmptyState.isVisible = false
                return@addLoadStateListener
            }

            // ---------------- SUCCESS ----------------
            binding.progressBar.isVisible = false

            if (isListEmpty) {
                binding.recyclerView.isVisible = false
                binding.llEmptyState.isVisible = true
                binding.btnRetry.isVisible = false   // No retry on empty success
                binding.textEmpty.text = context?.getString(R.string.no_articles_found)
            } else {
                binding.recyclerView.isVisible = true
                binding.llEmptyState.isVisible = false
            }
        }
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener {
            // Retry loading
            newsAdapter.retry()
            binding.progressBar.isVisible = true
            binding.llEmptyState.isVisible = false
            binding.recyclerView.isVisible = false
        }
    }

    private fun openArticleDetail(article: Article) {
        findNavController().navigate(
            R.id.articleDetailFragment, bundleOf("arg_article" to article)
        )
    }

    private fun setupObservers() {
        // Use Business news for HomeFragment
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.businessNewsPagingData.collectLatest { pagingData ->
                newsAdapter.submitData(pagingData)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.getAllBookmarkedUrls().collect { urls ->
                newsAdapter.updateBookmarkedUrls(urls)
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