package com.proxynode.feedbackapp.presentation.screen.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proxynode.feedbackapp.data.model.FeedbackEntity
import com.proxynode.feedbackapp.data.model.FeedbackStats
import com.proxynode.feedbackapp.data.repository.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val allFeedback: List<FeedbackEntity> = emptyList(),
    val filteredFeedback: List<FeedbackEntity> = emptyList(),
    val stats: FeedbackStats? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminViewModel(context: Context) : ViewModel() {

    private val repository = FeedbackRepository(context)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load feedback
                repository.getAllFeedback().collect { feedbackList ->
                    _uiState.value = _uiState.value.copy(
                        allFeedback = feedbackList,
                        filteredFeedback = feedbackList,
                        isLoading = false
                    )
                }

                // Load stats
                val stats = repository.getFeedbackStats()
                _uiState.value = _uiState.value.copy(stats = stats)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun filterFeedback(category: String) {
        val filtered = if (category == "All") {
            _uiState.value.allFeedback
        } else {
            _uiState.value.allFeedback.filter { it.tag == category }
        }

        _uiState.value = _uiState.value.copy(filteredFeedback = filtered)
    }

    fun generateTestData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                repository.generateTestData()

                // Refresh stats
                val stats = repository.getFeedbackStats()
                _uiState.value = _uiState.value.copy(
                    stats = stats,
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to generate test data: ${e.message}"
                )
            }
        }
    }
}