package com.donnations.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.donnations.core.common.HasCoroutineExceptionHandler
import com.donnations.core.common.viewModelReportingScope
import com.donnations.domain.home.repository.HomeRepository
import com.donnations.feature.home.model.HomeUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    mapper: HomeUiMapper,
    private val savedStateHandle: SavedStateHandle,
    override val exceptionHandler: CoroutineExceptionHandler,
) : ViewModel(), HasCoroutineExceptionHandler {

    private val loadRequests = MutableStateFlow(0)
    private val selectedCategoryId = savedStateHandle.getStateFlow(SELECTED_CATEGORY_KEY, HomeUiMapper.ALL_CATEGORY_ID)

    // Shows the stored home right away and refreshes it in the background;
    // the error screen only appears when the refresh fails with nothing stored.
    val uiState: StateFlow<HomeUiState> = loadRequests
        .flatMapLatest {
            channelFlow {
                val refreshState = MutableStateFlow(Refresh.Running)
                launch { refreshState.value = refresh() }
                combine(repository.observeHome(), refreshState, selectedCategoryId) { cached, refresh, categoryId ->
                    when {
                        cached != null -> HomeUiState.Success(mapper.map(cached.value, categoryId))
                        refresh == Refresh.Failed -> HomeUiState.Error(R.string.home_error_load)
                        else -> HomeUiState.Loading
                    }
                }.collect { send(it) }
            }
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

    // Failures are already reported by the repository.
    private suspend fun refresh(): Refresh = try {
        repository.refreshHome()
        Refresh.Done
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Refresh.Failed
    }

    private enum class Refresh { Running, Done, Failed }

    private companion object {
        const val SELECTED_CATEGORY_KEY = "selectedCategoryId"
    }
}
