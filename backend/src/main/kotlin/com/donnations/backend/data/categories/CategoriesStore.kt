package com.donnations.backend.data.categories

import com.donnations.backend.domain.model.Category

interface CategoriesStore {
    fun getAll(): List<Category>
}
