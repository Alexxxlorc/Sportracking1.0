package com.example.sportracking.core

sealed class ResponseService<out T> {
    data class Success<T>(val data: T): ResponseService <T>()
    data class Error(val data: String): ResponseService<Nothing>()
    object Loading: ResponseService<Nothing>()
}