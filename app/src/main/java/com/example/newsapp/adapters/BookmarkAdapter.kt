package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.databinding.BookmarkedArticlesCardBinding
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.utils.DateFormatter

class BookmarkAdapter(
    private val onReadMoreClick: (BookmarkedArticle) -> Unit,
    private val toggleBookmark: (BookmarkedArticle) -> Unit,
    private val onShareClick: (BookmarkedArticle) -> Unit,
    private val dateFormatter: DateFormatter
) : ListAdapter<BookmarkedArticle, BookmarkAdapter.BookmarkViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BookmarkedArticle>() {
            override fun areItemsTheSame(oldItem: BookmarkedArticle, newItem: BookmarkedArticle) =
                oldItem.url == newItem.url

            override fun areContentsTheSame(
                oldItem: BookmarkedArticle,
                newItem: BookmarkedArticle
            ) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BookmarkViewHolder(
        BookmarkedArticlesCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onReadMoreClick,
        toggleBookmark,
        onShareClick,
        dateFormatter
    )

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) =
        holder.bind(getItem(position))

    class BookmarkViewHolder(
        private val binding: BookmarkedArticlesCardBinding,
        private val onReadMoreClick: (BookmarkedArticle) -> Unit,
        private val onBookmarkClick: (BookmarkedArticle) -> Unit,
        private val onShareClick: (BookmarkedArticle) -> Unit,
        private val dateFormatter: DateFormatter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookmarkedArticle) {
            binding.tvNewsTitle.text = item.title
            binding.tvNewsSource.text = item.sourceName
            binding.tvNewsTime.text = dateFormatter.getTimeAgo(item.bookmarkedAt)

            // Load image
            item.urlToImage?.let { url ->
                Glide.with(binding.root.context)
                    .load(url)
                    .placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_newspaper)
                    .into(binding.ivNewsImage)
            } ?: run {
                binding.ivNewsImage.setImageResource(R.drawable.ic_newspaper)
            }

            // Bookmark icon filled
            binding.ivBookmark.setImageResource(R.drawable.ic_bookmark)
            binding.ivBookmark.setColorFilter(binding.root.context.getColor(R.color.blueMain))

            // Click listeners
            binding.tvReadMore.setOnClickListener { onReadMoreClick(item) }
            binding.ivBookmark.setOnClickListener { onBookmarkClick(item) }
            binding.ivShare.setOnClickListener { onShareClick(item) }
        }
    }
}
