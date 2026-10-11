@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.donnations.feature.campaign

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donnations.core.designsystem.component.FundingProgress
import com.donnations.core.designsystem.component.LoadingWheel
import com.donnations.core.designsystem.component.RemoteImage
import com.donnations.feature.campaign.model.CampaignDetailUiModel

@Composable
fun CampaignDetailRoute(
    campaignId: String,
    onBack: () -> Unit,
    onDonate: () -> Unit = {},
    viewModel: CampaignDetailViewModel =
        hiltViewModel<CampaignDetailViewModel, CampaignDetailViewModel.Factory> { it.create(campaignId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CampaignDetailScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        onBack = onBack,
        onDonate = onDonate
    )
}

@Composable
fun CampaignDetailScreen(
    uiState: CampaignDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onDonate: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.campaign_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(stringResource(R.string.campaign_detail_back))
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            CampaignDetailUiState.Loading -> LoadingWheel(Modifier.fillMaxSize().padding(padding))
            is CampaignDetailUiState.Error -> ErrorContent(uiState.message, onRetry, Modifier.fillMaxSize().padding(padding))
            is CampaignDetailUiState.Success -> CampaignContent(uiState.campaign, onDonate, padding)
        }
    }
}

@Composable
private fun CampaignContent(campaign: CampaignDetailUiModel, onDonate: () -> Unit, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RemoteImage(
                url = campaign.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(280.dp),
            )
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(campaign.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(campaign.summary, style = MaterialTheme.typography.bodyLarge)
                FundingProgress(campaign.raised, campaign.goal, campaign.percent, campaign.progress, amountsAboveBar = true)
                Text(campaign.donorCount, style = MaterialTheme.typography.bodyMedium)
                Text(campaign.endsAt, style = MaterialTheme.typography.bodyMedium)
                Text(campaign.organizer, style = MaterialTheme.typography.bodyMedium)
                Text(campaign.location, style = MaterialTheme.typography.bodyMedium)
                Text(campaign.description, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onDonate, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.campaign_detail_donate))
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(@StringRes message: Int, onRetry: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Button(onClick = onRetry) { Text(stringResource(R.string.campaign_detail_retry)) }
    }
}
