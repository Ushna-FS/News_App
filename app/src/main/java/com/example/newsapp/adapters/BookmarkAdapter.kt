package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.models.getFormattedDate
import com.example.newsapp.databinding.ItemNewsArticleBinding

class BookmarkAdapter(
    private val onItemClick: (Article) -> Unit,
    private val onBookmarkClick: (Article) -> Unit
) : ListAdapter<Article, BookmarkAdapter.BookmarkViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemNewsArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookmarkViewHolder(binding, onItemClick, onBookmarkClick)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookmarkViewHolder(
        private val binding: ItemNewsArticleBinding,
        private val onItemClick: (Article) -> Unit,
        private val onBookmarkClick: (Article) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.tvNewsTitle.text = article.title
            binding.tvNewsDescription.text = article.description ?: ""
            binding.tvNewsSource.text = article.source.name ?: "Unknown"
            binding.tvNewsTime.text = article.getFormattedDate()

            // Load image (same logic)
            article.urlToImage?.let { url ->
                Glide.with(binding.root.context)
                    .load(url)
                    .placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_newspaper)
                    .into(binding.ivNewsImage)
            } ?: run {
                binding.ivNewsImage.setImageResource(R.drawable.ic_newspaper)
            }

            // Bookmark icon filled (same logic)
            binding.ivBookmark.setImageResource(R.drawable.ic_bookmark)
            binding.ivBookmark.setColorFilter(
                binding.root.context.getColor(R.color.blueMain)
            )

            // Click listeners (same logic)
            binding.root.setOnClickListener {
                onItemClick(article)
            }

            binding.ivBookmark.setOnClickListener {
                onBookmarkClick(article)
            }
        }
    }
}
