package com.example.newsapp.data.models

import android.os.Parcelable
import com.example.newsapp.utils.Category
import kotlinx.parcelize.Parcelize


data class NewsResponse(
    val status: String, val totalResults: Int, val articles: List<Article>
)

@Parcelize
data class Article(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?

) : Parcelable

@Parcelize
data class Source(
    val id: String?, val name: String
) : Parcelable

fun Article.getFormattedDate(): String {
    return publishedAt?.substringBefore("T") ?: ""
}

fun Article.getCategory(): Category {

    val source = source.name.lowercase()
    val title = title.lowercase()
    val desc = description?.lowercase() ?: ""
    val content = content?.lowercase() ?: ""

    val text = "$source $title $desc $content"

    return when {
        // ENERGY
        text.contains("energy")
                || text.contains("oil")
                || text.contains("gas")
                || text.contains("renewable")
                || text.contains("battery")
                || text.contains("solar")
                || text.contains("power grid")
                || text.contains("energy storage")
            -> Category.Energy

        // FINANCE
        text.contains("stock")
                || text.contains("market")
                || text.contains("finance")
                || text.contains("economy")
                || text.contains("investment")
                || text.contains("revenue")
                || text.contains("profit")
                || text.contains("shares")
                || text.contains("valuation")
                || text.contains("billion")
                || text.contains("trillion")
                || source.contains("bloomberg")
                || source.contains("financial times")
                || source.contains("yahoo finance")
                || source.contains("fox business")
            -> Category.Finance

        // POLITICS
        text.contains("government")
                || text.contains("president")
                || text.contains("minister")
                || text.contains("parliament")
                || text.contains("senate")
                || text.contains("election")
                || text.contains("policy")
                || text.contains("law")
                || text.contains("regulator")
                || text.contains("license")
                || source.contains("politico")
                || source.contains("independent")
            -> Category.Politics

        // TECHNOLOGY
        text.contains("ai")
                || text.contains("technology")
                || text.contains("software")
                || text.contains("robot")
                || text.contains("autopilot")
                || text.contains("self-driving")
                || text.contains("robotaxi")
                || text.contains("carplay")
                || text.contains("iphone")
                || text.contains("android")
                || text.contains("startup")
                || text.contains("tesla")
                || text.contains("spacex")
                || source.contains("techcrunch")
                || source.contains("the verge")
                || source.contains("wired")
                || source.contains("techradar")
                || source.contains("github")
            -> Category.Technology

        // BUSINESS
        text.contains("business")
                || text.contains("company")
                || text.contains("ceo")
                || text.contains("corporate")
                || text.contains("industry")
                || text.contains("jobs")
                || text.contains("factory")
                || source.contains("business insider")
            -> Category.Business

        // HEALTH
        text.contains("health")
                || text.contains("medical")
                || text.contains("medicine")
                || text.contains("hospital")
                || text.contains("doctor")
                || text.contains("vaccine")
                || text.contains("covid")
                || text.contains("virus")
                || text.contains("treatment")
            -> Category.Health

        // SPORTS
        text.contains("football")
                || text.contains("cricket")
                || text.contains("match")
                || text.contains("league")
                || text.contains("tournament")
                || text.contains("goal")
                || text.contains("nba")
                || text.contains("fifa")
                || text.contains("olympics")
                || source.contains("espn")
            -> Category.Sports

        else -> Category.All
    }
}