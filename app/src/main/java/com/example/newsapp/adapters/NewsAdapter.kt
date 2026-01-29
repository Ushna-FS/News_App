package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.Data.models.Article

class NewsAdapter(
    private val articles: List<Article> = emptyList()
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivNewsImage)
        val titleTextView: TextView = itemView.findViewById(R.id.tvNewsTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvNewsDescription)
        val sourceTextView: TextView = itemView.findViewById(R.id.tvNewsSource)
        val timeTextView: TextView = itemView.findViewById(R.id.tvNewsTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_article, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]

        holder.titleTextView.text = article.title
        holder.descriptionTextView.text = article.description ?: "No description available"
        holder.sourceTextView.text = article.source.name

        // Format time (remove timezone part)
        val publishedTime = article.publishedAt.substringBefore("T")
        holder.timeTextView.text = publishedTime

        // Load image with Glide
        article.urlToImage?.let { imageUrl ->
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_newspaper) // Add a placeholder image
                .error(R.drawable.ic_error) // Add error image
                .into(holder.imageView)
        } ?: run {
            holder.imageView.setImageResource(R.drawable.ic_newspaper)
        }
    }

    override fun getItemCount(): Int = articles.size
}