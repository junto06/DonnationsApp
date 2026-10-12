package com.donnations.domain.home.repository

import com.donnations.core.model.Cached
import com.donnations.domain.home.model.Home
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    // The stored home (null until the first successful refresh), re-emitted after every refresh.
    fun observeHome(): Flow<Cached<Home>?>

    // Fetches home from the backend and stores it; throws when that fails, keeping the stored copy.
    suspend fun refreshHome()
}
