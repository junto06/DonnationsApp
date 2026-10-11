package com.donnations.feature.donation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.donnations.feature.donation.DonationRoute
import kotlinx.serialization.Serializable

// donnations://donate/{campaignId}
// must match the intent-filter on MainActivity in the app manifest.
const val DONATION_DEEP_LINK_BASE = "donnations://donate"

@Serializable
data class DonationDestination(val campaignId: String)

fun NavController.navigateToDonation(campaignId: String) {
    navigate(DonationDestination(campaignId))
}

fun NavGraphBuilder.donationScreen(onBack: () -> Unit) {
    composable<DonationDestination>(
        deepLinks = listOf(navDeepLink<DonationDestination>(basePath = DONATION_DEEP_LINK_BASE)),
    ) { entry ->
        DonationRoute(campaignId = entry.toRoute<DonationDestination>().campaignId, onBack = onBack)
    }
}
