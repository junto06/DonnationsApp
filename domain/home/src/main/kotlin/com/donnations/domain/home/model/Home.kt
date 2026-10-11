package com.donnations.domain.home.model

data class Home(
    val categories: List<Category>,
    val featured: List<Campaign>,
    val campaigns: List<Campaign>,
)
