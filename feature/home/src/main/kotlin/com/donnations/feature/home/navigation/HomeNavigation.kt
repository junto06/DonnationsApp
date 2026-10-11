package com.donnations.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.donnations.feature.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

fun NavGraphBuilder.homeScreen(onCampaignClick: (String) -> Unit) {
    composable<HomeDestination> {
        HomeRoute(onCampaignClick = onCampaignClick)
    }
}
