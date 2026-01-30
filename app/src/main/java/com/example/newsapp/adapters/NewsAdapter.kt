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
import java.text.SimpleDateFormat
import java.util.*

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
        holder.timeTextView.text = formatDate(article.publishedAt)

        // Load image with Glide
        article.urlToImage?.let { imageUrl ->
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_newspaper)
                .error(R.drawable.ic_error)
                .into(holder.imageView)
        } ?: run {
            holder.imageView.setImageResource(R.drawable.ic_newspaper)
        }
    }

    override fun getItemCount(): Int = articles.size

    private fun formatDate(publishedAt: String): String {
        return try {
            // Format: "2026-01-30T04:18:45Z" → "30 Jan, 4:18 AM"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(publishedAt)

            val outputFormat = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            publishedAt.substringBefore("T",e.toString()) // Fallback: just show date part
        }
    }
}