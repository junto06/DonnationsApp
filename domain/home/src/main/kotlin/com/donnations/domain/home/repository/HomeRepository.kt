package com.donnations.domain.home.repository

import com.donnations.core.model.Cached
import com.donnations.core.model.Data
import com.donnations.domain.home.model.Home
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    // The stored home right away, then refreshed from the backend (once per collection). Never throws.
    fun getHome(): Flow<Data<Cached<Home>>>
}
