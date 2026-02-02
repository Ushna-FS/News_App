package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.Data.models.Article
import com.example.newsapp.databinding.ItemNewsArticleBinding
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(
    private var articles: List<Article>,
    private val onItemClick: (Article) -> Unit = {}
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(val binding: ItemNewsArticleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]

        // Set data using ViewBinding
        holder.binding.tvNewsTitle.text = article.title
        holder.binding.tvNewsDescription.text = article.description ?: "No description available"
        holder.binding.tvNewsSource.text = article.source.name
        holder.binding.tvNewsTime.text = formatDate(article.publishedAt)

        // Load image with Glide
        article.urlToImage?.let { imageUrl ->
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(com.example.newsapp.R.drawable.ic_newspaper)
                .error(com.example.newsapp.R.drawable.ic_error)
                .into(holder.binding.ivNewsImage)
        } ?: run {
            holder.binding.ivNewsImage.setImageResource(com.example.newsapp.R.drawable.ic_newspaper)
        }
    }

    fun updateArticles(newArticles: List<Article>) {
        articles = newArticles
        notifyDataSetChanged()
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
            // Fallback: just show date part
            publishedAt.substringBefore("T")
        }
    }
}