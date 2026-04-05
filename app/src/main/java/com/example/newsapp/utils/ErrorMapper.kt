package com.example.newsapp.utils

import com.example.newsapp.R
import com.example.shared.data.models.NetworkError

fun mapErrorToMessage(error: Throwable): Int {

    return when (error) {

        is NetworkError.NoInternet -> R.string.no_internet_connection

        is NetworkError.RateLimit -> R.string.api_rate_limit_err

        is NetworkError.ServerError -> R.string.server_err

        is NetworkError.Unauthorized -> R.string.unauthorized

        is NetworkError.NotFound -> R.string.not_found

        else -> R.string.error_load_articles
    }
}
