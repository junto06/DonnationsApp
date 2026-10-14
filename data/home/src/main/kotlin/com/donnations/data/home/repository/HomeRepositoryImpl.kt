package com.donnations.data.home.repository

import com.donnations.core.base.HasLogger
import com.donnations.core.base.Logger
import com.donnations.core.base.logged
import com.donnations.core.base.observeAndRefresh
import com.donnations.core.model.Cached
import com.donnations.core.model.Data
import com.donnations.data.campaignstore.CampaignDao
import com.donnations.data.home.local.HomeDao
import com.donnations.data.home.local.toDomain
import com.donnations.data.home.local.toRefresh
import com.donnations.data.home.remote.HomeApi
import com.donnations.domain.home.model.Home
import com.donnations.domain.home.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
    private val homeDao: HomeDao,
    private val campaignDao: CampaignDao,
    private val clock: Clock,
    override val logger: Logger,
) : HomeRepository, HasLogger {

    override fun getHome(): Flow<Data<Cached<Home>>> = observeAndRefresh(
        fromStorage = storedHome(),
        // Mapped before storing, so a malformed response never replaces a good copy
        fromNetwork = {
            logged("Refreshing home") {
                api.getHome().toRefresh(clock.instant())
            }
        },
        toStorage = { refresh ->
            // Two databases, so not atomic: campaigns go first, so the layout never points at a missing campaign.
            // A crash in between leaves the old layout over updated campaigns, which still shows correctly.
            campaignDao.upsertSummaries(refresh.campaigns)
            homeDao.replace(refresh.home)
        },
    )

    // Home's layout from its own database, filled with campaigns from the shared store.
    private fun storedHome(): Flow<Cached<Home>?> =
        homeDao.observeHome().flatMapLatest { stored ->
            if (stored == null) return@flatMapLatest flowOf(null)
            campaignDao.observeCampaigns(stored.entries.map { it.campaignId }.distinct())
                .map { campaigns ->
                    logged("Reading stored home") {
                        stored.toDomain(campaigns)
                    }
                }
        }
}
