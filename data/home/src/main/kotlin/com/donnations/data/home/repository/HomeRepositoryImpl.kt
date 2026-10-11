package com.donnations.data.home.repository

import com.donnations.data.home.remote.HomeApi
import com.donnations.data.home.remote.toDomain
import com.donnations.domain.home.model.Home
import com.donnations.domain.home.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
) : HomeRepository {
    override fun getHome(): Flow<Home> = flow { emit(api.getHome().toDomain()) }
}
