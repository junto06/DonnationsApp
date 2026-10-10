package com.donnations.backend.http.dto

data class ErrorResponseDto(
    val message: String,
    val errorCode: String,
)
