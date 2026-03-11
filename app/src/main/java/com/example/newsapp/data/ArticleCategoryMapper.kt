package com.example.newsapp.data

import com.example.newsapp.data.models.Article

object ArticleCategoryMapper {

    val categories = listOf(
        "All",
        "Technology",
        "Business",
        "Finance",
        "Energy",
        "Politics",
        "Health",
        "Sports",
    )
    fun getCategory(article: Article): String {

        val source = article.source.name.lowercase()

        val title = article.title.lowercase()
        val desc = article.description?.lowercase() ?: ""
        val content = article.content?.lowercase() ?: ""

        val text = "$source $title $desc $content"

        return when {
            // ENERGY (put before tech to avoid EV misclassification)
            text.contains("energy")
                    || text.contains("oil")
                    || text.contains("gas")
                    || text.contains("renewable")
                    || text.contains("battery")
                    || text.contains("solar")
                    || text.contains("power grid")
                    || text.contains("energy storage")
                -> "Energy"

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
                -> "Finance"

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
                -> "Politics"

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
                -> "Technology"

            // BUSINESS
            text.contains("business")
                    || text.contains("company")
                    || text.contains("ceo")
                    || text.contains("corporate")
                    || text.contains("industry")
                    || text.contains("jobs")
                    || text.contains("factory")
                    || source.contains("business insider")
                -> "Business"

            // HEALTH
            text.contains("health")
                    || text.contains("medical")
                    || text.contains("hospital")
                    || text.contains("doctor")
                    || text.contains("skin-care")
                    || source.contains("medical")
                    || source.contains("who")
                -> "Health"

            // SPORTS
            text.contains("football")
                    || text.contains("cricket")
                    || text.contains("match")
                    || text.contains("fifa")
                    || text.contains("nba")
                    || source.contains("espn")
                -> "Sports"

            else -> "General"
        }
    }
}