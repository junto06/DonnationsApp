package com.donnations.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.donnations.core.common.HasCoroutineExceptionHandler
import com.donnations.core.common.viewModelReportingScope
import com.donnations.domain.home.repository.HomeRepository
import com.donnations.feature.home.model.HomeUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
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

    val uiState: StateFlow<HomeUiState> = loadRequests
        .flatMapLatest {
            repository.getHome()
                .combine<_, _, HomeUiState>(selectedCategoryId) { home, categoryId ->
                    HomeUiState.Success(mapper.map(home, categoryId))
                }
                .onStart { emit(HomeUiState.Loading) }
                .catch { emit(HomeUiState.Error(R.string.home_error_load)) }
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
