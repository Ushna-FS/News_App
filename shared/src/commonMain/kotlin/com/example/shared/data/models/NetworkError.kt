package com.example.shared.data.models

sealed class NetworkError : Exception() {

    class NoInternet : NetworkError()

    class RateLimit : NetworkError()

    class AllKeysExhausted : NetworkError()

    class ServerError : NetworkError()

    class Unauthorized : NetworkError()

    class NotFound : NetworkError()

    data class Unknown(val error: Throwable) : NetworkError()
}