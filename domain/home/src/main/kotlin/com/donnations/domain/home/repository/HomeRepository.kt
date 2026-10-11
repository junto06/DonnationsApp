package com.donnations.domain.home.repository

import com.donnations.domain.home.model.Home
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHome(): Flow<Home>
}
