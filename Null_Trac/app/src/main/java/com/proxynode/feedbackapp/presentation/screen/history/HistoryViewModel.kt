package com.proxynode.feedbackapp.presentation.screen.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proxynode.feedbackapp.data.model.FeedbackEntity
import com.proxynode.feedbackapp.data.repository.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val feedbackList: List<FeedbackEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HistoryViewModel(context: Context) : ViewModel() {

    private val repository = FeedbackRepository(context)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadFeedbackHistory()
    }

    private fun loadFeedbackHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                repository.getUserFeedback().collect { feedbackList ->
                    _uiState.value = _uiState.value.copy(
                        feedbackList = feedbackList,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                repository.clearAllFeedback()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to clear history: ${e.message}"
                )
            }
        }
    }
}