package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.data.models.Article
import com.example.newsapp.databinding.ItemNewsArticleBinding
import com.example.newsapp.databinding.ItemLoadingBinding
import java.text.SimpleDateFormat
import java.util.*

class NewsRecyclerAdapter(
    private var articles: List<Article> = emptyList(),
    private val onItemClick: (Article) -> Unit = {},
    private val onLoadMore: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private var isLoading = false
    private var hasMorePages = true

    fun setHasMorePages(hasMore: Boolean) {
        this.hasMorePages = hasMore
    }

    inner class ArticleViewHolder(val binding: ItemNewsArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) = with(binding) {

            tvNewsTitle.text = article.title
            tvNewsDescription.text =
                article.description ?: itemView.context.getString(R.string.no_description_available)
            tvNewsSource.text = article.source?.name ?: "Unknown" // FIXED: Added safe call
            tvNewsTime.text = formatDate(article.publishedAt)

            // Image loading
            (article.urlToImage ?: "").let { imageUrl ->
                Glide.with(itemView.context)
                    .load(imageUrl.ifEmpty { R.drawable.ic_newspaper })
                    .placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_error)
                    .into(ivNewsImage)
            }

            root.setOnClickListener {
                onItemClick(article)
            }
        }
    }

    class LoadingViewHolder(val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemNewsArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ArticleViewHolder(binding)
        } else {
            val binding = ItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            LoadingViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < articles.size) VIEW_TYPE_ITEM else VIEW_TYPE_LOADING
    }

    override fun getItemCount(): Int {
        return articles.size + if (isLoading) 1 else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ArticleViewHolder && position < articles.size) {
            holder.bind(articles[position])

            // Load more when reaching 5th last item
            if (position == articles.size - 5 && !isLoading && hasMorePages) {
                onLoadMore()
            }
        }
    }

    fun updateArticles(newArticles: List<Article>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            if (loading) {
                notifyItemInserted(articles.size)
            } else {
                notifyItemRemoved(articles.size)
            }
        }
    }

    private fun formatDate(publishedAt: String?): String {
        return try {
            if (publishedAt.isNullOrEmpty()) return ""

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(publishedAt) ?: return ""

            // Create output format with date and time
            val outputFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            // Fallback: try to extract just the date part
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