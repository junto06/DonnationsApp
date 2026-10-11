@file:OptIn(ExperimentalMaterial3Api::class)

package com.donnations.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donnations.core.designsystem.component.LoadingWheel
import com.donnations.feature.home.component.CampaignRow
import com.donnations.feature.home.component.CategoryTabs
import com.donnations.feature.home.component.FeaturedCarousel
import com.donnations.feature.home.model.HomeUiMapper
import com.donnations.feature.home.model.HomeUiModel

@Composable
fun HomeRoute(
    onCampaignClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        onCampaignClick = onCampaignClick
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onCampaignClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold)
                }
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            HomeUiState.Loading -> LoadingWheel(contentModifier)
            is HomeUiState.Error -> ErrorContent(uiState.message, onRetry, contentModifier)
            is HomeUiState.Success -> HomeContent(uiState.home, onCampaignClick, contentModifier)
        }
    }
}

@Composable
private fun HomeContent(
    home: HomeUiModel,
    onCampaignClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategoryId by rememberSaveable { mutableStateOf(HomeUiMapper.ALL_CATEGORY_ID) }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "categories") {
            CategoryTabs(
                categories = home.categories,
                selectedId = selectedCategoryId,
                onSelect = { selectedCategoryId = it },
            )
        }
        if (home.featured.isNotEmpty()) {
            item(key = "featured") {
                FeaturedCarousel(
                    campaigns = home.featured,
                    onDonateClick = onCampaignClick,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
        if (home.campaigns.isNotEmpty()) {
            item(key = "campaigns-header") {
                Text(
                    text = stringResource(R.string.home_campaigns_header),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            items(home.campaigns, key = { it.id }) { campaign ->
                CampaignRow(campaign = campaign, onClick = onCampaignClick)
            }
        }
    }
}

@Composable
private fun ErrorContent(
    @StringRes message: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.home_retry)) }
    }
}
