package com.proxynode.feedbackapp

import android.app.Application
import android.util.Log
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.data.models.SDKEnvironment
import com.runanywhere.sdk.public.extensions.addModelFromURL
import com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProxyNodeApplication : Application() {

    companion object {
        private const val TAG = "ProxyNodeApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize RunAnywhere SDK asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            initializeRunAnywhereSDK()
        }
    }

    private suspend fun initializeRunAnywhereSDK() {
        try {
            Log.i(TAG, "Initializing RunAnywhere SDK...")

            // Step 1: Initialize SDK
            RunAnywhere.initialize(
                context = this@ProxyNodeApplication,
                apiKey = "dev", // Development mode
                environment = SDKEnvironment.DEVELOPMENT
            )

            // Step 2: Register LLM Service Provider
            LlamaCppServiceProvider.register()

            // Step 3: Register Models for feedback analysis
            registerModels()

            // Step 4: Scan for previously downloaded models
            RunAnywhere.scanForDownloadedModels()

            Log.i(TAG, "RunAnywhere SDK initialized successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize RunAnywhere SDK: ${e.message}", e)
        }
    }

    private suspend fun registerModels() {
        try {
            // Lightweight model for text processing and anonymization
            addModelFromURL(
                url = "https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf",
                name = "SmolLM2 360M Q8_0",
                type = "LLM"
            )

            Log.i(TAG, "Models registered successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to register models: ${e.message}", e)
        }
    }
}