package com.donnations.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.donnations.feature.campaign.navigation.campaignDetailScreen
import com.donnations.feature.campaign.navigation.navigateToCampaignDetail
import com.donnations.feature.home.navigation.HomeDestination
import com.donnations.feature.home.navigation.homeScreen

@Composable
fun DonnationsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = HomeDestination, modifier = modifier) {
        homeScreen(onCampaignClick = navController::navigateToCampaignDetail)
        // navigateUp never pops the start destination, so a double-tapped Back can't leave a blank host.
        campaignDetailScreen(onBack = { navController.navigateUp() })
    }
}
