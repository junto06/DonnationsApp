package com.donnations.data.home.repository

import com.donnations.core.base.HasLogger
import com.donnations.core.base.Logger
import com.donnations.core.base.logged
import com.donnations.core.model.Cached
import com.donnations.data.home.local.HomeDao
import com.donnations.data.home.local.toDomain
import com.donnations.data.home.local.toStored
import com.donnations.data.home.remote.HomeApi
import com.donnations.data.home.remote.toDomain
import com.donnations.domain.home.model.Home
import com.donnations.domain.home.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
    private val dao: HomeDao,
    private val clock: Clock,
    override val logger: Logger,
) : HomeRepository, HasLogger {

    override fun observeHome(): Flow<Cached<Home>?> =
        dao.observeHome().map { stored ->
            logged("Reading stored home") {
                stored?.toDomain()
            }
        }

    override suspend fun refreshHome() {
        // Mapped to domain before storing, so a malformed response never replaces a good copy
        val home = logged("Refreshing home") {
            api.getHome().toDomain()
        }
        dao.replace(home.toStored(clock.instant()))
    }
}
