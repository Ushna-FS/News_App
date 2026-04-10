package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.data.models.Article
import com.example.newsapp.databinding.BookmarkedArticlesCardBinding
import com.example.newsapp.databinding.HomeArticlesCardBinding
import com.example.newsapp.databinding.ItemLoadingBinding
import com.example.newsapp.databinding.ItemNewsArticleBinding
import com.example.newsapp.utils.DateFormatter
import java.io.IOException
import java.net.SocketTimeoutException

enum class CardType {
    HOME, DISCOVER, BOOKMARK
}

class NewsPagingAdapter(
    private val cardType: CardType,
    private val onItemClick: (Article) -> Unit,
    private val onBookmarkClick: (Article) -> Unit,
    private val onExtractSource: ((Article) -> Unit)? = null,
    private val dateFormatter: DateFormatter

) : PagingDataAdapter<Article, RecyclerView.ViewHolder>(ARTICLE_COMPARATOR) {
    var currentCategory: String = "General"

    companion object {
        private const val VIEW_HOME = 1
        private const val VIEW_DISCOVER = 2
        private const val VIEW_BOOKMARK = 3

        private val ARTICLE_COMPARATOR = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (cardType) {
            CardType.HOME -> VIEW_HOME
            CardType.DISCOVER -> VIEW_DISCOVER
            CardType.BOOKMARK -> VIEW_BOOKMARK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            VIEW_HOME -> {
                val binding = HomeArticlesCardBinding.inflate(inflater, parent, false)
                HomeViewHolder(binding)
            }

            VIEW_BOOKMARK -> {
                val binding = BookmarkedArticlesCardBinding.inflate(inflater, parent, false)
                BookmarkCardViewHolder(binding)
            }

            else -> {
                val binding = ItemNewsArticleBinding.inflate(inflater, parent, false)
                DiscoverViewHolder(
                    binding, this, onItemClick, onBookmarkClick, onExtractSource, dateFormatter
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val article = getItem(position) ?: return

        when (holder) {
            is HomeViewHolder -> holder.bind(article)
            is DiscoverViewHolder -> holder.bind(article)
            is BookmarkCardViewHolder -> holder.bind(article)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>
    ) {
        if (payloads.contains("BOOKMARK_STATE")) {
            val article = getItem(position) ?: return

            when (holder) {
                is DiscoverViewHolder -> {
                    val isBookmarked = bookmarkedUrls.contains(article.url)
                    holder.updateBookmarkIconVisual(isBookmarked)
                }

                is HomeViewHolder -> {
                    val isBookmarked = bookmarkedUrls.contains(article.url)
                    updateBookmarkIcon(holder.binding.ivBookmark, isBookmarked)
                }
            }
            return
        }

        super.onBindViewHolder(holder, position, payloads)
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

    private fun updateBookmarkIcon(view: ImageView, bookmarked: Boolean) {
        view.setImageResource(
            if (bookmarked) R.drawable.ic_bookmark
            else R.drawable.ic_bookmark_border
        )
        view.setColorFilter(view.context.getColor(R.color.blueMain))
    }


    var bookmarkedUrls: Set<String> = emptySet()

    fun updateBookmarkedUrls(urls: Set<String>) {
        bookmarkedUrls = urls
        notifyItemRangeChanged(0, itemCount, "BOOKMARK_STATE")
    }


    inner class HomeViewHolder(
        val binding: HomeArticlesCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.tvNewsTitle.text = article.title
            binding.tvNewsDescription.text = article.description ?: ""
            binding.tvNewsSource.text = article.source.name
            binding.tvNewsTime.text = dateFormatter.formatDisplayDate(article.publishedAt)

            val isBookmarked = bookmarkedUrls.contains(article.url)
            updateBookmarkIcon(binding.ivBookmark, isBookmarked)

            binding.chipCategory.text = currentCategory.replaceFirstChar { it.uppercase() }


            binding.root.setOnClickListener { onItemClick(article) }
            binding.ivBookmark.setOnClickListener { onBookmarkClick(article) }
            binding.tvReadMore.setOnClickListener { onItemClick(article) }
        }
    }

    class DiscoverViewHolder(
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
                Glide.with(binding.root.context).load(url).placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_newspaper).into(binding.ivNewsImage)
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

    inner class BookmarkCardViewHolder(
        private val binding: BookmarkedArticlesCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {

            binding.tvNewsTitle.text = article.title
            binding.tvNewsSource.text = article.source.name
            binding.tvNewsTime.text = dateFormatter.formatDisplayDate(article.publishedAt)

            Glide.with(binding.root).load(article.urlToImage).placeholder(R.drawable.ic_newspaper)
                .into(binding.ivNewsImage)

            binding.root.setOnClickListener { onItemClick(article) }
            binding.ivBookmark.setOnClickListener { onBookmarkClick(article) }
        }
    }

    class NewsLoadStateAdapter(private val retry: () -> Unit) :
        LoadStateAdapter<NewsLoadStateAdapter.LoadStateViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup, loadState: LoadState
        ): LoadStateViewHolder {
            val binding = ItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

            return LoadStateViewHolder(binding, retry)
        }

        override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
            holder.bind(loadState)
        }


        class LoadStateViewHolder(
            private val binding: ItemLoadingBinding, private val retry: () -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(loadState: LoadState) {

                binding.progressBar.isVisible = loadState is LoadState.Loading
                binding.textError.isVisible = loadState is LoadState.Error
                binding.buttonRetry.isVisible = loadState is LoadState.Error

                if (loadState is LoadState.Error) {
                    val error = loadState.error
                    val ctx = binding.root.context

                    binding.textError.text = when (error) {
                        is IOException, is SocketTimeoutException -> ctx.getString(R.string.error_no_internet)

                        else -> ctx.getString(R.string.error_generic)
                    }

                    binding.buttonRetry.setOnClickListener { retry() }
                }

                if (loadState is LoadState.NotLoading && loadState.endOfPaginationReached) {
                    binding.progressBar.isVisible = false
                    binding.textError.isVisible = true
                    binding.textError.text = binding.root.context.getString(R.string.pagination_end)
                    binding.buttonRetry.isVisible = false
                }
            }

        }
    }
}