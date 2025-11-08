package com.proxynode.feedbackapp.presentation.screen.feedback

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    viewModel: FeedbackViewModel = viewModel { FeedbackViewModel(LocalContext.current) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val microphonePermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Share Your Feedback",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your feedback is completely anonymous and processed locally on your device. Help us improve by sharing your thoughts.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // AI Status Card
        AIStatusCard(
            isInitialized = uiState.isAIInitialized,
            isInitializing = uiState.isInitializingAI,
            onRetryInitialization = viewModel::initializeAI
        )

        // Feedback Input
        OutlinedTextField(
            value = uiState.feedbackText,
            onValueChange = viewModel::updateFeedbackText,
            label = { Text("Your feedback") },
            placeholder = { Text("Share your thoughts about academics, infrastructure, placements...") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            maxLines = 8,
            enabled = !uiState.isSubmitting
        )

        // Voice Input Section
        if (microphonePermission.status.isGranted) {
            VoiceInputSection(
                isRecording = uiState.isRecording,
                onStartRecording = viewModel::startVoiceRecording,
                onStopRecording = viewModel::stopVoiceRecording,
                enabled = !uiState.isSubmitting && uiState.isAIInitialized
            )
        } else {
            VoicePermissionSection(
                onRequestPermission = { microphonePermission.launchPermissionRequest() },
                shouldShowRationale = microphonePermission.status.shouldShowRationale
            )
        }

        // Submit Button
        Button(
            onClick = viewModel::submitFeedback,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSubmit
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Processing...")
            } else {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Feedback")
            }
        }

        // Status Messages
        AnimatedVisibility(
            visible = uiState.statusMessage.isNotBlank(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (uiState.statusType) {
                        StatusType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                        StatusType.ERROR -> MaterialTheme.colorScheme.errorContainer
                        StatusType.INFO -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    text = uiState.statusMessage,
                    modifier = Modifier.padding(16.dp),
                    color = when (uiState.statusType) {
                        StatusType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                        StatusType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                        StatusType.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // Privacy Notice
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Privacy Guarantee",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• All processing happens on your device\n• No data is sent to external servers\n• Personal identifiers are automatically removed\n• Your feedback remains completely anonymous",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AIStatusCard(
    isInitialized: Boolean,
    isInitializing: Boolean,
    onRetryInitialization: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                isInitialized -> MaterialTheme.colorScheme.primaryContainer
                isInitializing -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        isInitialized -> "AI Ready"
                        isInitializing -> "Initializing AI..."
                        else -> "AI Not Ready"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = when {
                        isInitialized -> "Feedback analysis is ready"
                        isInitializing -> "Setting up on-device AI model..."
                        else -> "AI model needs to be initialized"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isInitialized && !isInitializing) {
                TextButton(onClick = onRetryInitialization) {
                    Text("Retry")
                }
            } else if (isInitializing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun VoiceInputSection(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    enabled: Boolean
) {
    Card {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isRecording) "Recording..." else "Voice Input",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isRecording) "Tap to stop recording" else "Tap to record your feedback",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = if (isRecording) onStopRecording else onStartRecording,
                enabled = enabled
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun VoicePermissionSection(
    onRequestPermission: () -> Unit,
    shouldShowRationale: Boolean
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Voice Input",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (shouldShowRationale) {
                    "Microphone permission is needed for voice feedback. Your voice is processed locally and never sent anywhere."
                } else {
                    "Enable voice input to speak your feedback instead of typing."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}