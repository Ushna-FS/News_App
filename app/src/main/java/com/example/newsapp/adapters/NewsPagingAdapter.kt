package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.data.models.Article
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class NewsPagingAdapter(
    private val onItemClick: (Article) -> Unit,
    private val onBookmarkClick: (Article) -> Unit,
    private val onExtractSource: ((Article) -> Unit)? = null,

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

        return ArticleViewHolder(view, this, onItemClick, onBookmarkClick, onExtractSource)

    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)

        }
    }

    fun updateBookmarkIconForUrl(url: String, isBookmarked: Boolean) {
        for (i in 0 until itemCount) {
            getItem(i)?.let { article ->
                if (article.url == url) {
                    article.isBookmarked = isBookmarked
                    notifyItemChanged(i)
                    return
                }
            }
        }
    }
    var bookmarkedUrls: Set<String> = emptySet()

    fun updateBookmarkedUrls(urls: Set<String>) {
        bookmarkedUrls = urls
        notifyDataSetChanged()
    }


    class ArticleViewHolder(
        itemView: View,
        private val adapter: NewsPagingAdapter,
        private val onItemClick: (Article) -> Unit,
        private val onBookmarkClick: (Article) -> Unit,
        private val onExtractSource: ((Article) -> Unit)? = null,

        ) : RecyclerView.ViewHolder(itemView) {

        private val imageNews = itemView.findViewById<android.widget.ImageView>(R.id.ivNewsImage)
        private val textTitle = itemView.findViewById<android.widget.TextView>(R.id.tvNewsTitle)
        private val textDescription =
            itemView.findViewById<android.widget.TextView>(R.id.tvNewsDescription)
        private val textSource = itemView.findViewById<android.widget.TextView>(R.id.tvNewsSource)
        private val textTime = itemView.findViewById<android.widget.TextView>(R.id.tvNewsTime)
        private val bookmarkIcon = itemView.findViewById<android.widget.ImageView>(R.id.ivBookmark)

        fun bind(article: Article) {
            textTitle.text = article.title ?: ""
            textDescription.text = article.description ?: ""
            textSource.text = article.source?.name ?: "Unknown"
            textTime.text = formatDate(article.publishedAt)

            // Extract source for filter
            onExtractSource?.invoke(article)
            //bookmark state
            val isBookmarkedNow = adapter.bookmarkedUrls.contains(article.url)
            article.isBookmarked = isBookmarkedNow
            updateBookmarkIconVisual(isBookmarkedNow)


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

            itemView.setOnClickListener {
                onItemClick(article)
            }
            bookmarkIcon.setOnClickListener {
                onBookmarkClick(article)
            }

        }

        private fun updateBookmarkIconVisual(isBookmarked: Boolean) {
            if (isBookmarked) {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark)
                bookmarkIcon.setColorFilter(itemView.context.getColor(R.color.blueMain))
            } else {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)
                bookmarkIcon.setColorFilter(itemView.context.getColor(R.color.blueMain))
            }
        }


        private fun formatDate(publishedAt: String?): String {
            return try {
                if (publishedAt.isNullOrEmpty()) return ""

                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(publishedAt) ?: return ""

                val outputFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
                outputFormat.format(date)
            } catch (e: Exception) {
                try {
                    publishedAt?.substringBefore("T")?.let { datePart ->
                        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = dateOnlyFormat.parse(datePart)
                        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        outputFormat.format(date ?: Date())
                    } ?: ""
                } catch (e: Exception) {
                    publishedAt?.substringBefore("T") ?: ""
                }
            }
        }
    }

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

                        val error = loadState.error

                        textError.text = when (error) {
                            is IOException,
                            is SocketTimeoutException -> "No internet connection — tap retry"

                            else -> "Something went wrong — tap retry"
                        }

                        buttonRetry.setOnClickListener { retry() }
                    }

                    is LoadState.NotLoading -> {
                        if (loadState.endOfPaginationReached) {
                            progressBar.isVisible = false
                            textError.isVisible = true
                            textError.text = "You reached the end — no more articles"
                            buttonRetry.isVisible = false
                        } else {
                            progressBar.isVisible = false
                            textError.isVisible = false
                            buttonRetry.isVisible = false
                        }
                    }
                }
            }
        }
    }
}