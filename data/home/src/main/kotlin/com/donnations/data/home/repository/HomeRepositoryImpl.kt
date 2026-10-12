package com.donnations.data.home.repository

import com.donnations.core.base.HasLogger
import com.donnations.core.base.Logger
import com.donnations.core.base.logged
import com.donnations.data.home.remote.HomeApi
import com.donnations.data.home.remote.toDomain
import com.donnations.domain.home.model.Home
import com.donnations.domain.home.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
    override val logger: Logger,
) : HomeRepository, HasLogger {
    override fun getHome(): Flow<Home> =
        flow {
            val home = logged("Loading home") {
                api.getHome().toDomain()
            }
            emit(home)
        }
}
