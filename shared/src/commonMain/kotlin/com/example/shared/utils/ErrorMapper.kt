package com.example.shared.utils

import com.example.shared.data.models.NetworkError
import io.ktor.client.plugins.*
import kotlinx.io.IOException

object ErrorMapper {

    fun mapToNetworkError(throwable: Throwable): NetworkError {
        return when (throwable) {

            is NetworkError -> throwable

            // Network issues (no internet, socket, etc.)
            is IOException -> NetworkError.NoInternet()

            is HttpRequestTimeoutException -> NetworkError.NoInternet()

            // 4xx errors
            is ClientRequestException -> {
                when (throwable.response.status.value) {
                    401 -> NetworkError.Unauthorized()
                    404 -> NetworkError.NotFound()
                    429 -> NetworkError.RateLimit()
                    else -> NetworkError.Unknown(throwable)
                }
            }

            // 5xx errors
            is ServerResponseException -> NetworkError.ServerError()

            // Other Ktor errors
            is ResponseException -> NetworkError.Unknown(throwable)

            else -> NetworkError.Unknown(throwable)
        }
    }
}