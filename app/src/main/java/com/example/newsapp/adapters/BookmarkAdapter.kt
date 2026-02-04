package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.data.models.Article

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
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_news_article, parent, false)
        return BookmarkViewHolder(view, onItemClick, onBookmarkClick)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookmarkViewHolder(
        itemView: View,
        private val onItemClick: (Article) -> Unit,
        private val onBookmarkClick: (Article) -> Unit
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

            // Load image
            article.urlToImage?.let { url ->
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(R.drawable.ic_newspaper)
                    .error(R.drawable.ic_newspaper)
                    .into(imageNews)
            } ?: run {
                imageNews.setImageResource(R.drawable.ic_newspaper)
            }

            // Set bookmark icon to filled
            bookmarkIcon.setImageResource(R.drawable.ic_bookmark)
            bookmarkIcon.setColorFilter(
                itemView.context.getColor(R.color.blueMain)
            )

            // Set click listeners
            itemView.setOnClickListener {
                onItemClick(article)
            }

            bookmarkIcon.setOnClickListener {
                // Toggle visual state immediately
                val currentDrawable = bookmarkIcon.drawable
                val isCurrentlyFilled = currentDrawable.constantState?.equals(
                    itemView.context.getDrawable(R.drawable.ic_bookmark)?.constantState
                ) == true

                bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)
                bookmarkIcon.setColorFilter(itemView.context.getColor(R.color.blueMain))

                // Show toast
                Toast.makeText(itemView.context, "Article removed from bookmarks", Toast.LENGTH_SHORT).show()

                // Perform bookmark operation
                onBookmarkClick(article)
            }
            bookmarkIcon.setOnClickListener {
                // Update visual immediately
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)
                bookmarkIcon.setColorFilter(itemView.context.getColor(R.color.blueMain))

                // Show toast
                Toast.makeText(itemView.context, "Article removed from bookmarks", Toast.LENGTH_SHORT).show()

                // Notify parent
                onBookmarkClick(article)
            }
        }
    }
}