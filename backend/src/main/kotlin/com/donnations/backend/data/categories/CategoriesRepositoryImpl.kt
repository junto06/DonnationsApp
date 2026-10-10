package com.donnations.backend.data.categories

import com.donnations.backend.domain.model.Category
import com.donnations.backend.domain.repository.CategoriesRepository
import org.springframework.stereotype.Repository

@Repository
class CategoriesRepositoryImpl(
    private val categoriesStore: CategoriesStore,
) : CategoriesRepository {
    override fun getAll(): List<Category> = categoriesStore.getAll()
}
