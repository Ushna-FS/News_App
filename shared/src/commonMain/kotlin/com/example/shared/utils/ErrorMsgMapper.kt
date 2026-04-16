package com.example.shared.utils

import com.example.shared.data.models.NetworkError
import me.sample.library.resources.Res
import me.sample.library.resources.*
import org.jetbrains.compose.resources.StringResource

fun mapErrorToMessage(error: NetworkError): StringResource {

    return when (error) {

        is NetworkError.NoInternet -> Res.string.no_internet_connection

        is NetworkError.RateLimit -> Res.string.api_rate_limit_err

        is NetworkError.AllKeysExhausted -> Res.string.all_api_keys_exhausted

        is NetworkError.ServerError -> Res.string.server_err

        is NetworkError.Unauthorized -> Res.string.unauthorized

        is NetworkError.NotFound -> Res.string.not_found

        else -> Res.string.error_load_articles
    }
}
