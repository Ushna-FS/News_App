package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.ViewModels.NewsViewModel
import com.example.newsapp.data.models.Article
import kotlinx.coroutines.launch

class NewsPagingAdapter(
    private val onItemClick: (Article) -> Unit,
    private val onExtractSource: ((Article) -> Unit)? = null,
    private val viewModel: NewsViewModel? = null,
    private val lifecycleOwner: LifecycleOwner? = null
) : PagingDataAdapter<Article, NewsPagingAdapter.ArticleViewHolder>(ARTICLE_COMPARATOR) {

    companion object {
        private val ARTICLE_COMPARATOR = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_news_article, parent, false)
        return ArticleViewHolder(view, onItemClick, onExtractSource, viewModel, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class ArticleViewHolder(
        itemView: View,
        private val onItemClick: (Article) -> Unit,
        private val onExtractSource: ((Article) -> Unit)?,
        private val viewModel: NewsViewModel?,
        private val lifecycleOwner: LifecycleOwner?
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageNews = itemView.findViewById<android.widget.ImageView>(R.id.ivNewsImage)
        private val textTitle = itemView.findViewById<android.widget.TextView>(R.id.tvNewsTitle)
        private val textDescription = itemView.findViewById<android.widget.TextView>(R.id.tvNewsDescription)
        private val textSource = itemView.findViewById<android.widget.TextView>(R.id.tvNewsSource)
        private val textTime = itemView.findViewById<android.widget.TextView>(R.id.tvNewsTime)
        private val bookmarkIcon = itemView.findViewById<android.widget.ImageView>(R.id.ivBookmark)

        fun bind(article: Article) {
            textTitle.text = article.title ?: ""
            textDescription.text = article.description ?: ""
            textSource.text = article.source?.name ?: "Unknown"
            textTime.text = article.publishedAt?.take(10) ?: ""

            // Extract source for filter
            onExtractSource?.invoke(article)

            // Load image with Glide
            article.urlToImage?.let { url ->
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_newspaper)
                    .into(imageNews)
            } ?: run {
                imageNews.setImageResource(R.drawable.ic_newspaper)
            }

            // Set bookmark state
            updateBookmarkIcon(article.url)

            // Set click listeners
            itemView.setOnClickListener {
                onItemClick(article)
            }

            bookmarkIcon.setOnClickListener {
                viewModel?.toggleBookmark(article)
                showBookmarkToast(article)
            }
        }
        private fun showBookmarkToast(article: Article) {
            // Check current bookmark state
            val url = article.url
            if (url != null) {
                lifecycleOwner?.lifecycleScope?.launch {
                    viewModel?.isArticleBookmarked(url)?.collect { isBookmarked ->
                        val message = if (isBookmarked) {
                            "Article removed from bookmarks"
                        } else {
                            "Article added to bookmarks"
                        }
                        Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun updateBookmarkIcon(url: String?) {
            if (viewModel != null && lifecycleOwner != null && !url.isNullOrEmpty()) {
                lifecycleOwner.lifecycleScope.launch {
                    viewModel.isArticleBookmarked(url).collect { isBookmarked ->
                        if (isBookmarked) {
                            bookmarkIcon.setImageResource(R.drawable.ic_bookmark)
                            bookmarkIcon.setColorFilter(
                                itemView.context.getColor(R.color.blueMain)
                            )
                        } else {
                            bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)
                            bookmarkIcon.setColorFilter(
                                itemView.context.getColor(R.color.blueMain)
                            )
                        }
                    }
                }
            }
        }
    }
    // SEPARATE LoadStateAdapter for footer
    class NewsLoadStateAdapter(private val retry: () -> Unit) :
        LoadStateAdapter<NewsLoadStateAdapter.LoadStateViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            loadState: LoadState
        ): LoadStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
            return LoadStateViewHolder(view, retry)
        }

        override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
            holder.bind(loadState)
        }

        class LoadStateViewHolder(
            itemView: View,
            private val retry: () -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            private val progressBar =
                itemView.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            private val textError =
                itemView.findViewById<android.widget.TextView>(R.id.textError)
            private val buttonRetry =
                itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonRetry)

            fun bind(loadState: LoadState) {
                when (loadState) {
                    is LoadState.Loading -> {
                        progressBar.isVisible = true
                        textError.isVisible = false
                        buttonRetry.isVisible = false
                    }
                    is LoadState.Error -> {
                        progressBar.isVisible = false
                        textError.isVisible = true
                        buttonRetry.isVisible = true

                        textError.text = loadState.error.message ?: "Unknown error"
                        buttonRetry.setOnClickListener { retry() }
                    }
                    else -> {
                        progressBar.isVisible = false
                        textError.isVisible = false
                        buttonRetry.isVisible = false
                    }
                }
            }
        }
    }
}


