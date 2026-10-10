package com.donnations.backend.domain.repository

import com.donnations.backend.domain.model.Category

interface CategoriesRepository {
    fun getAll(): List<Category>
}
