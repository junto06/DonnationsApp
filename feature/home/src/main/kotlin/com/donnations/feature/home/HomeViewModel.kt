package com.donnations.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.donnations.core.common.HasCoroutineExceptionHandler
import com.donnations.core.common.viewModelReportingScope
import com.donnations.core.base.fold
import com.donnations.core.base.mapContent
import com.donnations.domain.home.repository.HomeRepository
import com.donnations.feature.home.model.HomeUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: HomeRepository,
    mapper: HomeUiMapper,
    private val savedStateHandle: SavedStateHandle,
    override val exceptionHandler: CoroutineExceptionHandler,
) : ViewModel(), HasCoroutineExceptionHandler {

    private val loadRequests = MutableStateFlow(0)
    private val selectedCategoryId = savedStateHandle.getStateFlow(SELECTED_CATEGORY_KEY, HomeUiMapper.ALL_CATEGORY_ID)

    // Stored home first, refreshed in the background; the error screen only appears when the refresh fails with nothing stored.
    // The repository never throws and mapContent turns mapper failures into errors, so no catch.
    val uiState: StateFlow<HomeUiState> = loadRequests
        .flatMapLatest {
            repository.getHome().combine(selectedCategoryId) { home, categoryId ->
                home.mapContent { mapper.map(it.value, categoryId) }.fold(
                    onLoading = { HomeUiState.Loading },
                    onError = { HomeUiState.Error(R.string.home_error_load) },
                    onContent = { HomeUiState.Success(it) },
                )
            }
        }
        .stateIn(
            viewModelReportingScope,
            SharingStarted.WhileSubscribed(5_000),
            HomeUiState.Loading
        )

    fun selectCategory(categoryId: String) {
        savedStateHandle[SELECTED_CATEGORY_KEY] = categoryId
    }

    fun retry() {
        loadRequests.update { it + 1 }
    }

    private companion object {
        const val SELECTED_CATEGORY_KEY = "selectedCategoryId"
    }
}
