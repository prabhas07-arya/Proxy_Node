package com.proxynode.feedbackapp.presentation.screen.feedback

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proxynode.feedbackapp.data.repository.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class StatusType {
    SUCCESS, ERROR, INFO
}

data class FeedbackUiState(
    val feedbackText: String = "",
    val isSubmitting: Boolean = false,
    val isRecording: Boolean = false,
    val isAIInitialized: Boolean = false,
    val isInitializingAI: Boolean = false,
    val statusMessage: String = "",
    val statusType: StatusType = StatusType.INFO
) {
    val canSubmit: Boolean
        get() = feedbackText.trim().isNotEmpty() && !isSubmitting && isAIInitialized
}

class FeedbackViewModel(context: Context) : ViewModel() {

    private val repository = FeedbackRepository(context)

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        initializeAI()
    }

    fun initializeAI() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isInitializingAI = true,
                statusMessage = "Initializing AI model for feedback analysis...",
                statusType = StatusType.INFO
            )

            try {
                val success = repository.initializeAI()

                _uiState.value = _uiState.value.copy(
                    isInitializingAI = false,
                    isAIInitialized = success,
                    statusMessage = if (success) {
                        "AI model ready! You can now submit feedback."
                    } else {
                        "Failed to initialize AI model. Some features may not work."
                    },
                    statusType = if (success) StatusType.SUCCESS else StatusType.ERROR
                )

                // Clear status message after delay
                delay(3000)
                clearStatus()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isInitializingAI = false,
                    isAIInitialized = false,
                    statusMessage = "Error initializing AI: ${e.message}",
                    statusType = StatusType.ERROR
                )

                delay(5000)
                clearStatus()
            }
        }
    }

    fun updateFeedbackText(text: String) {
        _uiState.value = _uiState.value.copy(feedbackText = text)
    }

    fun submitFeedback() {
        val currentText = _uiState.value.feedbackText.trim()
        if (currentText.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                statusMessage = "Processing your feedback...",
                statusType = StatusType.INFO
            )

            try {
                val result = repository.submitFeedback(currentText, _uiState.value.isRecording)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        feedbackText = "",
                        statusMessage = "Thank you! Your feedback has been submitted anonymously.",
                        statusType = StatusType.SUCCESS
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        statusMessage = "Failed to submit feedback: ${result.exceptionOrNull()?.message}",
                        statusType = StatusType.ERROR
                    )
                }

                delay(3000)
                clearStatus()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    statusMessage = "Error submitting feedback: ${e.message}",
                    statusType = StatusType.ERROR
                )

                delay(5000)
                clearStatus()
            }
        }
    }

    fun startVoiceRecording() {
        // Note: Actual voice recording implementation would require speech-to-text integration
        // For now, this is a placeholder that simulates voice input
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            statusMessage = "Voice recording started (simulated)",
            statusType = StatusType.INFO
        )

        viewModelScope.launch {
            delay(1000)
            clearStatus()
        }
    }

    fun stopVoiceRecording() {
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            statusMessage = "Voice recording stopped (simulated)",
            statusType = StatusType.INFO
        )

        // Simulate converting voice to text
        viewModelScope.launch {
            delay(2000)

            // Add simulated voice input text
            val simulatedVoiceText =
                "This is a simulated voice input about improving the campus facilities."
            _uiState.value = _uiState.value.copy(
                feedbackText = _uiState.value.feedbackText + if (_uiState.value.feedbackText.isNotEmpty()) " " else "" + simulatedVoiceText,
                statusMessage = "Voice converted to text",
                statusType = StatusType.SUCCESS
            )

            delay(2000)
            clearStatus()
        }
    }

    private fun clearStatus() {
        _uiState.value = _uiState.value.copy(statusMessage = "")
    }
}