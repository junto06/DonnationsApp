package com.donnations.backend.domain.model

@JvmInline
value class CategoryId(val value: String)

data class Category(
    val id: CategoryId,
    val name: String,
) {
    init {
        require(id.value.isNotBlank()) { "Category id must not be blank" }
        require(name.isNotBlank()) { "Category name must not be blank" }
    }
}
