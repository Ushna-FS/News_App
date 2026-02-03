package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.data.models.Article

class NewsPagingAdapter(
    private val onItemClick: (Article) -> Unit,
    private val onExtractSource: ((Article) -> Unit)? = null
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
        return ArticleViewHolder(view, onItemClick, onExtractSource)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class ArticleViewHolder(
        itemView: android.view.View,
        private val onItemClick: (Article) -> Unit,
        private val onExtractSource: ((Article) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(article: Article) {

            val imageNews = itemView.findViewById<android.widget.ImageView>(R.id.ivNewsImage)
            val textTitle = itemView.findViewById<android.widget.TextView>(R.id.tvNewsTitle)
            val textDescription = itemView.findViewById<android.widget.TextView>(R.id.tvNewsDescription)
            val textSource = itemView.findViewById<android.widget.TextView>(R.id.tvNewsSource)
            val textTime = itemView.findViewById<android.widget.TextView>(R.id.tvNewsTime)

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

            itemView.setOnClickListener {
                onItemClick(article)
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