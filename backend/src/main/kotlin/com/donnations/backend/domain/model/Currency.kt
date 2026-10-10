package com.donnations.backend.domain.model

enum class Currency(val code: String, val fractionDigits: Int) {
    USD("USD", 2),
}
