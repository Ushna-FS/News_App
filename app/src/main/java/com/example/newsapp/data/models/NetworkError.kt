package com.example.newsapp.data.models

sealed class NetworkError : Exception() {

    class NoInternet : NetworkError()

    class RateLimit : NetworkError() // 429

    class ServerError : NetworkError() // 500+

    class Unauthorized : NetworkError() // 401

    class NotFound : NetworkError() // 404

    class PaginationEnd : NetworkError()

    data class Unknown(val error: Throwable) : NetworkError()
}