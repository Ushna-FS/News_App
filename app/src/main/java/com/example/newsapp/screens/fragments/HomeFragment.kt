package com.example.newsapp.screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsPagingAdapter
import com.example.newsapp.databinding.FragmentHomeBinding
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.data.models.Article
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val newsViewModel: NewsViewModel by activityViewModels()
    private lateinit var newsAdapter: NewsPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        binding.textWelcome.text = getString(R.string.home_user)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsPagingAdapter(
            onItemClick = { article ->
                openArticleDetail(article)
            },
            onExtractSource = { article ->
                newsViewModel.extractSourceFromArticle(article)
            },
            viewModel = newsViewModel,
            lifecycleOwner = viewLifecycleOwner
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter.withLoadStateFooter(
                footer = NewsPagingAdapter.NewsLoadStateAdapter { newsAdapter.retry() }
            )
            setHasFixedSize(true)
        }
    }

    private fun openArticleDetail(article: Article) {
        val fragmentManager = requireActivity().supportFragmentManager

        // Check if already showing to prevent duplicates
        if (fragmentManager.findFragmentByTag("ArticleDetail") != null) return

        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            .replace(
                R.id.fragment_container,
                ArticleDetailFragment.newInstance(article),
                "ArticleDetail"
            )
            .addToBackStack("ArticleDetail")
            .commit()
    }

    private fun setupObservers() {
        // Use Business news for HomeFragment
        viewLifecycleOwner.lifecycleScope.launch {
            newsViewModel.businessNewsPagingData.collectLatest { pagingData ->
                newsAdapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            newsAdapter.loadStateFlow.collectLatest { loadState ->
                binding.apply {
                    // Handle initial loading
                    when (loadState.refresh) {
                        is LoadState.Loading -> {
                            progressBar.isVisible = true
                            recyclerView.isVisible = false
                            footerLoading.root.isVisible = false
                        }

                        is LoadState.Error -> {
                            progressBar.isVisible = false
                            textEmpty.text = getString(
                                R.string.error,
                                (loadState.refresh as LoadState.Error).error.message
                            )
                            textEmpty.isVisible = true
                            recyclerView.isVisible = false
                            footerLoading.root.isVisible = false
                        }

                        else -> {
                            progressBar.isVisible = false
                            textEmpty.isVisible = false
                            recyclerView.isVisible = true
                        }
                    }

                    // Handle pagination loading
                    when (loadState.append) {
                        is LoadState.Loading -> {
                            footerLoading.root.isVisible = true
                        }

                        is LoadState.Error -> {
                            footerLoading.root.isVisible = true
                            footerLoading.textError.text =
                                (loadState.append as LoadState.Error).error.message
                            footerLoading.textError.isVisible = true
                            footerLoading.buttonRetry.isVisible = true
                            footerLoading.buttonRetry.setOnClickListener {
                                newsAdapter.retry()
                            }
                        }

                        else -> {
                            footerLoading.root.isVisible = false
                        }
                    }

                    // Show empty state
                    if (loadState.refresh !is LoadState.Loading &&
                        loadState.append.endOfPaginationReached &&
                        newsAdapter.itemCount == 0
                    ) {
                        textEmpty.text = getString(R.string.no_articles_found)
                        textEmpty.isVisible = true
                        recyclerView.isVisible = false
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}