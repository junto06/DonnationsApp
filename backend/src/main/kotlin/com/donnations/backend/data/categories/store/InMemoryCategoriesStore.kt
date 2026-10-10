package com.donnations.backend.data.categories.store

import com.donnations.backend.data.categories.CategoriesStore
import com.donnations.backend.domain.model.Category
import com.donnations.backend.domain.model.CategoryId
import org.springframework.stereotype.Component

@Component
class InMemoryCategoriesStore(
    private val categories: List<Category> = seedCategories,
) : CategoriesStore {
    override fun getAll(): List<Category> = categories
}

internal object SeedCategoryIds {
    val education = CategoryId("education")
    val health = CategoryId("health")
    val emergency = CategoryId("emergency")
    val water = CategoryId("water")
}

private val seedCategories = listOf(
    Category(SeedCategoryIds.education, "Education"),
    Category(SeedCategoryIds.health, "Health"),
    Category(SeedCategoryIds.emergency, "Emergency"),
    Category(SeedCategoryIds.water, "Clean Water"),
)
