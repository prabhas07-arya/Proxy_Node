package com.proxynode.feedbackapp.data.ai

import android.util.Log
import com.proxynode.feedbackapp.data.model.FeedbackResult
import com.proxynode.feedbackapp.data.model.FeedbackTag
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.listAvailableModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedbackAnalyzer {

    companion object {
        private const val TAG = "FeedbackAnalyzer"
        private const val MODEL_ID = "SmolLM2 360M Q8_0"
    }

    private var isModelLoaded = false

    suspend fun initializeModel(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isModelLoaded) return@withContext true

            Log.i(TAG, "Initializing feedback analysis model...")

            // Check if model is available
            val models = listAvailableModels()
            val targetModel = models.find { it.name == MODEL_ID }

            if (targetModel == null) {
                Log.e(TAG, "Model $MODEL_ID not found")
                return@withContext false
            }

            // Download if not already downloaded
            if (!targetModel.isDownloaded) {
                Log.i(TAG, "Downloading model...")
                RunAnywhere.downloadModel(targetModel.id).collect { progress ->
                    Log.d(TAG, "Download progress: ${(progress * 100).toInt()}%")
                }
            }

            // Load model
            Log.i(TAG, "Loading model...")
            val success = RunAnywhere.loadModel(targetModel.id)

            if (success) {
                isModelLoaded = true
                Log.i(TAG, "Model loaded successfully")
            } else {
                Log.e(TAG, "Failed to load model")
            }

            return@withContext success

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing model: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun analyzeFeedback(originalText: String): FeedbackResult =
        withContext(Dispatchers.IO) {
            try {
                if (!isModelLoaded) {
                    val initialized = initializeModel()
                    if (!initialized) {
                        return@withContext FeedbackResult(
                            originalText = originalText,
                            anonymizedText = anonymizeTextFallback(originalText),
                            summary = summarizeTextFallback(originalText),
                            tag = classifyTextFallback(originalText)
                        )
                    }
                }

                // Step 1: Anonymize the text
                val anonymizedText = anonymizeText(originalText)

                // Step 2: Summarize the feedback
                val summary = summarizeText(anonymizedText)

                // Step 3: Classify into categories
                val tag = classifyText(anonymizedText)

                return@withContext FeedbackResult(
                    originalText = originalText,
                    anonymizedText = anonymizedText,
                    summary = summary,
                    tag = tag
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing feedback: ${e.message}", e)

                // Fallback to rule-based analysis
                return@withContext FeedbackResult(
                    originalText = originalText,
                    anonymizedText = anonymizeTextFallback(originalText),
                    summary = summarizeTextFallback(originalText),
                    tag = classifyTextFallback(originalText)
                )
            }
        }

    private suspend fun anonymizeText(text: String): String {
        try {
            val prompt = """
                Remove all personal identifiers from this feedback while keeping the core message intact.
                Remove names, ID numbers, specific locations, phone numbers, email addresses, and any personally identifiable information.
                Replace them with generic terms like [STUDENT], [TEACHER], [LOCATION], etc.
                
                Original feedback: "$text"
                
                Anonymized feedback:
            """.trimIndent()

            val response = RunAnywhere.generate(prompt)
            return response.trim().takeIf { it.isNotBlank() } ?: anonymizeTextFallback(text)

        } catch (e: Exception) {
            Log.w(TAG, "AI anonymization failed, using fallback", e)
            return anonymizeTextFallback(text)
        }
    }

    private suspend fun summarizeText(text: String): String {
        try {
            val prompt = """
                Summarize this student feedback in exactly one clear, concise sentence that captures the main issue or suggestion.
                Keep it under 100 characters and make it actionable.
                
                Feedback: "$text"
                
                One-line summary:
            """.trimIndent()

            val response = RunAnywhere.generate(prompt)
            return response.trim().takeIf { it.isNotBlank() } ?: summarizeTextFallback(text)

        } catch (e: Exception) {
            Log.w(TAG, "AI summarization failed, using fallback", e)
            return summarizeTextFallback(text)
        }
    }

    private suspend fun classifyText(text: String): String {
        try {
            val prompt = """
                Classify this student feedback into exactly one category: "Academics", "Infrastructure", or "Placement".
                
                - Academics: courses, teaching, exams, curriculum, faculty, learning materials
                - Infrastructure: buildings, facilities, wifi, labs, library, hostel, canteen
                - Placement: jobs, internships, career guidance, industry connections, recruitment
                
                Feedback: "$text"
                
                Category (respond with exactly one word - Academics, Infrastructure, or Placement):
            """.trimIndent()

            val response = RunAnywhere.generate(prompt).trim()

            return when {
                response.contains(
                    "Academics",
                    ignoreCase = true
                ) -> FeedbackTag.ACADEMICS.displayName

                response.contains(
                    "Infrastructure",
                    ignoreCase = true
                ) -> FeedbackTag.INFRASTRUCTURE.displayName

                response.contains(
                    "Placement",
                    ignoreCase = true
                ) -> FeedbackTag.PLACEMENT.displayName

                else -> classifyTextFallback(text)
            }

        } catch (e: Exception) {
            Log.w(TAG, "AI classification failed, using fallback", e)
            return classifyTextFallback(text)
        }
    }

    // Fallback methods using simple rule-based approach
    private fun anonymizeTextFallback(text: String): String {
        var anonymized = text

        // Simple regex patterns for common identifiers
        anonymized =
            anonymized.replace(Regex("\\b[A-Z][a-z]+ [A-Z][a-z]+\\b"), "[STUDENT]") // Names
        anonymized = anonymized.replace(Regex("\\b\\d{6,}\\b"), "[ID]") // ID numbers
        anonymized = anonymized.replace(Regex("\\b\\d{10}\\b"), "[PHONE]") // Phone numbers
        anonymized = anonymized.replace(
            Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"),
            "[EMAIL]"
        ) // Emails

        return anonymized
    }

    private fun summarizeTextFallback(text: String): String {
        // Simple extractive summarization - take first sentence or truncate
        val firstSentence = text.split('.', '!', '?').firstOrNull()?.trim()
        return when {
            firstSentence != null && firstSentence.length in 10..100 -> firstSentence
            text.length <= 100 -> text.trim()
            else -> text.take(97).trim() + "..."
        }
    }

    private fun classifyTextFallback(text: String): String {
        val lowerText = text.lowercase()

        val academicKeywords = listOf(
            "teacher",
            "professor",
            "course",
            "exam",
            "study",
            "class",
            "subject",
            "lecture",
            "assignment",
            "grade",
            "syllabus"
        )
        val infrastructureKeywords = listOf(
            "building",
            "room",
            "wifi",
            "internet",
            "lab",
            "library",
            "hostel",
            "canteen",
            "facility",
            "maintenance",
            "equipment"
        )
        val placementKeywords = listOf(
            "job",
            "placement",
            "internship",
            "company",
            "interview",
            "career",
            "recruitment",
            "industry",
            "skill"
        )

        val academicScore = academicKeywords.count { lowerText.contains(it) }
        val infrastructureScore = infrastructureKeywords.count { lowerText.contains(it) }
        val placementScore = placementKeywords.count { lowerText.contains(it) }

        return when {
            academicScore >= infrastructureScore && academicScore >= placementScore -> FeedbackTag.ACADEMICS.displayName
            infrastructureScore >= placementScore -> FeedbackTag.INFRASTRUCTURE.displayName
            placementScore > 0 -> FeedbackTag.PLACEMENT.displayName
            else -> FeedbackTag.OTHER.displayName
        }
    }
}