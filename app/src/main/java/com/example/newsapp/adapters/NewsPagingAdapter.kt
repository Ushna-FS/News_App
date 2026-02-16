package com.example.newsapp.adapters

import android.view.LayoutInflater
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
import com.example.newsapp.databinding.ItemLoadingBinding
import com.example.newsapp.databinding.ItemNewsArticleBinding
import com.example.newsapp.utils.DateFormatter
import java.io.IOException
import java.net.SocketTimeoutException


class NewsPagingAdapter(
    private val onItemClick: (Article) -> Unit,
    private val onBookmarkClick: (Article) -> Unit,
    private val onExtractSource: ((Article) -> Unit)? = null,
    private val dateFormatter: DateFormatter

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

        val binding = ItemNewsArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ArticleViewHolder(
            binding,
            this,
            onItemClick,
            onBookmarkClick,
            onExtractSource,
            dateFormatter
        )

    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)

        }
    }


    override fun onBindViewHolder(
        holder: ArticleViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains("BOOKMARK_STATE")) {
            getItem(position)?.let { article ->
                val isBookmarked = bookmarkedUrls.contains(article.url)
                holder.updateBookmarkIconVisual(isBookmarked)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun updateBookmarkIconForUrl(url: String) {
        for (i in 0 until itemCount) {
            getItem(i)?.let { article ->
                if (article.url == url) {
                    notifyItemChanged(i, "BOOKMARK_STATE")
                    return
                }
            }
        }
    }

    var bookmarkedUrls: Set<String> = emptySet()

    fun updateBookmarkedUrls(urls: Set<String>) {
        bookmarkedUrls = urls
        notifyItemRangeChanged(0, itemCount, "BOOKMARK_STATE")
    }


    class ArticleViewHolder(
        private val binding: ItemNewsArticleBinding,
        private val adapter: NewsPagingAdapter,
        private val onItemClick: (Article) -> Unit,
        private val onBookmarkClick: (Article) -> Unit,
        private val onExtractSource: ((Article) -> Unit)? = null,
        private val dateFormatter: DateFormatter

    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.tvNewsTitle.text = article.title
            binding.tvNewsDescription.text = article.description ?: ""
            binding.tvNewsSource.text = article.source.name
            binding.tvNewsTime.text = dateFormatter.formatDisplayDate(article.publishedAt)

            // Extract source for filter
            onExtractSource?.invoke(article)
            //bookmark state
            val isBookmarkedNow = adapter.bookmarkedUrls.contains(article.url)
            updateBookmarkIconVisual(isBookmarkedNow)


            // Load image with Glide
            article.urlToImage?.let { url ->
                Glide.with(binding.root.context)
                    .load(url)
                    .placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_newspaper)
                    .into(binding.ivNewsImage)
            } ?: run {
                binding.ivNewsImage.setImageResource(R.drawable.ic_newspaper)
            }

            binding.root.setOnClickListener {
                onItemClick(article)
            }

            binding.ivBookmark.setOnClickListener {
                onBookmarkClick(article)
            }
        }

        fun updateBookmarkIconVisual(isBookmarked: Boolean) {
            if (isBookmarked) {
                binding.ivBookmark.setImageResource(R.drawable.ic_bookmark)
                binding.ivBookmark.setColorFilter(itemView.context.getColor(R.color.blueMain))
            } else {
                binding.ivBookmark.setImageResource(R.drawable.ic_bookmark_border)
                binding.ivBookmark.setColorFilter(itemView.context.getColor(R.color.blueMain))
            }
        }
    }

    class NewsLoadStateAdapter(private val retry: () -> Unit) :
        LoadStateAdapter<NewsLoadStateAdapter.LoadStateViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            loadState: LoadState
        ): LoadStateViewHolder {
            val binding = ItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            return LoadStateViewHolder(binding, retry)
        }

        override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
            holder.bind(loadState)
        }


        class LoadStateViewHolder(
            private val binding: ItemLoadingBinding,
            private val retry: () -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(loadState: LoadState) {

                binding.progressBar.isVisible = loadState is LoadState.Loading
                binding.textError.isVisible = loadState is LoadState.Error
                binding.buttonRetry.isVisible = loadState is LoadState.Error

                if (loadState is LoadState.Error) {
                    val error = loadState.error

                    binding.textError.text = when (error) {
                        is IOException,
                        is SocketTimeoutException ->
                            "No internet connection — tap retry"

                        else ->
                            "Something went wrong — tap retry"
                    }

                    binding.buttonRetry.setOnClickListener { retry() }
                }

                if (loadState is LoadState.NotLoading &&
                    loadState.endOfPaginationReached
                ) {
                    binding.progressBar.isVisible = false
                    binding.textError.isVisible = true
                    binding.textError.text = "You reached the end — no more articles"
                    binding.buttonRetry.isVisible = false
                }
            }

        }
    }
}