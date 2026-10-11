@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.donnations.feature.donation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donnations.core.designsystem.component.LoadingWheel
import com.donnations.feature.donation.model.DonationUiModel

@Composable
fun DonationRoute(
    campaignId: String,
    onBack: () -> Unit,
    viewModel: DonationViewModel =
        hiltViewModel<DonationViewModel, DonationViewModel.Factory> { it.create(campaignId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DonationScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onAmountChange = viewModel::changeAmount,
        onDonate = viewModel::donate,
    )
}

@Composable
fun DonationScreen(
    uiState: DonationUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAmountChange: (String) -> Unit,
    onDonate: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.donation_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text(stringResource(R.string.donation_back)) }
                },
            )
        },
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        when (uiState) {
            DonationUiState.Loading -> LoadingWheel(contentModifier)
            is DonationUiState.Error -> ErrorContent(uiState.message, uiState.retryable, onRetry, contentModifier)
            is DonationUiState.Success -> DonationContent(uiState.donation, onAmountChange, onDonate, contentModifier)
            is DonationUiState.Donated -> DonatedContent(uiState.message, onDone = onBack, contentModifier)
        }
    }
}

@Composable
private fun DonationContent(
    donation: DonationUiModel,
    onAmountChange: (String) -> Unit,
    onDonate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(donation.campaignTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.donation_choose_amount), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            donation.presets.forEach { preset ->
                FilterChip(
                    selected = preset.selected,
                    onClick = { onAmountChange(preset.inputValue) },
                    enabled = !donation.isSubmitting,
                    label = { Text(preset.label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        OutlinedTextField(
            value = donation.amount,
            onValueChange = onAmountChange,
            label = { Text(stringResource(R.string.donation_amount)) },
            prefix = { Text(donation.currencySymbol) },
            singleLine = true,
            enabled = !donation.isSubmitting,
            isError = donation.amountError != null,
            supportingText = donation.amountError?.let { error -> { Text(error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onDonate,
            enabled = donation.amountToDonate != null && !donation.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (donation.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.donation_donate))
            }
        }
        donation.submitError?.let { error ->
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DonatedContent(message: String, onDone: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.donation_done)) }
    }
}

@Composable
private fun ErrorContent(@StringRes message: Int, retryable: Boolean, onRetry: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (retryable) {
            Button(onClick = onRetry) { Text(stringResource(R.string.donation_retry)) }
        }
    }
}
