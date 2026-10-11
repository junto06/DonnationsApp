package com.donnations.feature.campaign.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.donnations.feature.campaign.CampaignDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class CampaignDetailDestination(val campaignId: String)

fun NavController.navigateToCampaignDetail(campaignId: String) {
    navigate(CampaignDetailDestination(campaignId))
}

fun NavGraphBuilder.campaignDetailScreen(onBack: () -> Unit) {
    composable<CampaignDetailDestination> { entry ->
        CampaignDetailRoute(campaignId = entry.toRoute<CampaignDetailDestination>().campaignId, onBack = onBack)
    }
}
