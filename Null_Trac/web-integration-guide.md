# RunAnywhere SDK - Web Integration Guide

## Overview

RunAnywhere SDK supports web deployment, enabling you to build AI-powered web applications that run
models entirely in the browser using WebAssembly (WASM).

## Key Features for Web

- 🌐 **Browser-native**: Runs in all modern browsers
- 🔒 **Privacy-first**: All processing happens client-side
- ⚡ **WebAssembly powered**: High-performance inference
- 📱 **Progressive Web App**: Can be installed like native apps
- 🚫 **No server required**: Complete offline functionality

## Technical Architecture

```
Browser (Client-Side)
├── JavaScript/TypeScript Application
├── RunAnywhere Web SDK
├── WebAssembly Runtime (WASM)
├── AI Models (Downloaded locally)
└── IndexedDB (Local storage)
```

## Getting Started

### 1. Installation

```bash
# Install via NPM
npm install @runanywhere/web-sdk

# Or via CDN
<script src="https://cdn.runanywhere.ai/sdk/web/latest/runanywhere.min.js"></script>
```

### 2. Basic Setup

```javascript
import { RunAnywhereWeb } from '@runanywhere/web-sdk';

// Initialize SDK
const sdk = new RunAnywhereWeb({
    apiKey: 'your-api-key',
    environment: 'development' // or 'production'
});

await sdk.initialize();
```

### 3. Model Management

```javascript
// Register models
await sdk.registerModel({
    id: 'smollm2-360m',
    name: 'SmolLM2 360M Q8_0',
    url: 'https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf',
    size: 119 * 1024 * 1024 // 119 MB
});

// Download model with progress
sdk.downloadModel('smollm2-360m', (progress) => {
    console.log(`Download progress: ${(progress * 100).toFixed(1)}%`);
});

// Load model for inference
await sdk.loadModel('smollm2-360m');
```

### 4. Text Generation

```javascript
// Simple generation
const response = await sdk.generate("What is artificial intelligence?");
console.log(response);

// Streaming generation
sdk.generateStream("Tell me a story").subscribe({
    next: (token) => {
        // Handle each token as it's generated
        document.getElementById('output').textContent += token;
    },
    complete: () => {
        console.log('Generation complete');
    },
    error: (error) => {
        console.error('Generation error:', error);
    }
});
```

## Web-Specific Considerations

### 1. CORS and Security Headers

For SharedArrayBuffer support (required for threading), you need these headers:

```html
<!-- Add to your HTML head -->
<meta http-equiv="Cross-Origin-Opener-Policy" content="same-origin">
<meta http-equiv="Cross-Origin-Embedder-Policy" content="require-corp">
```

Or configure your server:

```javascript
// Express.js example
app.use((req, res, next) => {
    res.setHeader('Cross-Origin-Opener-Policy', 'same-origin');
    res.setHeader('Cross-Origin-Embedder-Policy', 'require-corp');
    next();
});
```

### 2. Service Worker for GitHub Pages

If you can't set headers (like on GitHub Pages), use a service worker:

```javascript
// Install coi-serviceworker
npm install coi-serviceworker

// In your HTML
<script src="./node_modules/coi-serviceworker/coi-serviceworker.js"></script>
```

### 3. Progressive Web App (PWA)

```json
// manifest.json
{
    "name": "RunAnywhere AI Chat",
    "short_name": "AI Chat",
    "start_url": "/",
    "display": "standalone",
    "background_color": "#ffffff",
    "theme_color": "#007bff",
    "icons": [
        {
            "src": "icon-192.png",
            "sizes": "192x192",
            "type": "image/png"
        }
    ]
}
```

## Example Implementation

### React Component

```jsx
import React, { useState, useEffect } from 'react';
import { RunAnywhereWeb } from '@runanywhere/web-sdk';

function AIChat() {
    const [sdk, setSdk] = useState(null);
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        initializeSDK();
    }, []);

    const initializeSDK = async () => {
        try {
            const runAnywhere = new RunAnywhereWeb({
                apiKey: process.env.REACT_APP_RUNANYWHERE_API_KEY,
                environment: 'development'
            });

            await runAnywhere.initialize();
            
            // Register and load model
            await runAnywhere.registerModel({
                id: 'smollm2-360m',
                name: 'SmolLM2 360M Q8_0',
                url: 'https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf'
            });

            await runAnywhere.loadModel('smollm2-360m');
            setSdk(runAnywhere);
            
        } catch (error) {
            console.error('SDK initialization failed:', error);
        }
    };

    const sendMessage = async () => {
        if (!input.trim() || !sdk) return;

        const userMessage = { text: input, isUser: true };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setLoading(true);

        try {
            let response = '';
            sdk.generateStream(input).subscribe({
                next: (token) => {
                    response += token;
                    setMessages(prev => {
                        const newMessages = [...prev];
                        const lastMessage = newMessages[newMessages.length - 1];
                        
                        if (lastMessage && !lastMessage.isUser) {
                            lastMessage.text = response;
                        } else {
                            newMessages.push({ text: response, isUser: false });
                        }
                        
                        return newMessages;
                    });
                },
                complete: () => {
                    setLoading(false);
                },
                error: (error) => {
                    console.error('Generation error:', error);
                    setLoading(false);
                }
            });

        } catch (error) {
            console.error('Message send error:', error);
            setLoading(false);
        }
    };

    return (
        <div className="ai-chat">
            <div className="messages">
                {messages.map((message, index) => (
                    <div key={index} className={`message ${message.isUser ? 'user' : 'assistant'}`}>
                        {message.text}
                    </div>
                ))}
                {loading && <div className="message assistant">Thinking...</div>}
            </div>
            
            <div className="input-section">
                <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                    placeholder="Type your message..."
                    disabled={!sdk || loading}
                />
                <button onClick={sendMessage} disabled={!sdk || loading}>
                    Send
                </button>
            </div>
        </div>
    );
}

export default AIChat;
```

### Vue.js Component

```vue
<template>
  <div class="ai-chat">
    <div class="messages" ref="messagesContainer">
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message', message.isUser ? 'user' : 'assistant']"
      >
        {{ message.text }}
      </div>
      <div v-if="loading" class="message assistant">Thinking...</div>
    </div>
    
    <div class="input-section">
      <input
        v-model="input"
        @keypress.enter="sendMessage"
        :disabled="!sdk || loading"
        placeholder="Type your message..."
      />
      <button @click="sendMessage" :disabled="!sdk || loading">
        Send
      </button>
    </div>
  </div>
</template>

<script>
import { RunAnywhereWeb } from '@runanywhere/web-sdk';

export default {
  name: 'AIChat',
  data() {
    return {
      sdk: null,
      messages: [],
      input: '',
      loading: false
    };
  },
  
  async mounted() {
    await this.initializeSDK();
  },
  
  methods: {
    async initializeSDK() {
      try {
        this.sdk = new RunAnywhereWeb({
          apiKey: process.env.VUE_APP_RUNANYWHERE_API_KEY,
          environment: 'development'
        });

        await this.sdk.initialize();
        await this.loadModel();
        
      } catch (error) {
        console.error('SDK initialization failed:', error);
      }
    },
    
    async loadModel() {
      await this.sdk.registerModel({
        id: 'smollm2-360m',
        name: 'SmolLM2 360M Q8_0',
        url: 'https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf'
      });

      await this.sdk.loadModel('smollm2-360m');
    },
    
    async sendMessage() {
      if (!this.input.trim() || !this.sdk) return;

      this.messages.push({ text: this.input, isUser: true });
      const userInput = this.input;
      this.input = '';
      this.loading = true;

      try {
        let response = '';
        this.sdk.generateStream(userInput).subscribe({
          next: (token) => {
            response += token;
            
            // Update or create assistant message
            const lastMessage = this.messages[this.messages.length - 1];
            if (lastMessage && !lastMessage.isUser) {
              lastMessage.text = response;
            } else {
              this.messages.push({ text: response, isUser: false });
            }
          },
          complete: () => {
            this.loading = false;
            this.scrollToBottom();
          },
          error: (error) => {
            console.error('Generation error:', error);
            this.loading = false;
          }
        });

      } catch (error) {
        console.error('Message send error:', error);
        this.loading = false;
      }
    },
    
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        container.scrollTop = container.scrollHeight;
      });
    }
  }
};
</script>
```

## Deployment Options

### 1. Static Hosting (Recommended)

- **Vercel**: Automatic HTTPS and headers support
- **Netlify**: Easy deployment with build optimization
- **GitHub Pages**: Free hosting (requires service worker for headers)
- **AWS S3 + CloudFront**: Scalable static hosting

### 2. Example Deployment (Vercel)

```json
// vercel.json
{
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        {
          "key": "Cross-Origin-Opener-Policy",
          "value": "same-origin"
        },
        {
          "key": "Cross-Origin-Embedder-Policy",
          "value": "require-corp"
        }
      ]
    }
  ]
}
```

## Performance Optimization

### 1. Lazy Loading

```javascript
// Lazy load models only when needed
const loadModelOnDemand = async (modelId) => {
    if (!sdk.isModelLoaded(modelId)) {
        showLoading('Loading AI model...');
        await sdk.loadModel(modelId);
        hideLoading();
    }
};
```

### 2. Model Caching

```javascript
// Check if model is already cached
const isModelCached = await sdk.isModelDownloaded('smollm2-360m');
if (!isModelCached) {
    // Download with progress indicator
    await sdk.downloadModel('smollm2-360m', updateProgressBar);
}
```

### 3. Web Workers for Heavy Operations

```javascript
// Use Web Workers for model operations
const modelWorker = new Worker('model-worker.js');

modelWorker.postMessage({
    action: 'loadModel',
    modelId: 'smollm2-360m'
});

modelWorker.onmessage = (event) => {
    if (event.data.action === 'modelLoaded') {
        // Model ready for inference
        enableChat();
    }
};
```

## Browser Compatibility

| Browser | Version | WebAssembly | SharedArrayBuffer | Notes |
|---------|---------|-------------|-------------------|-------|
| Chrome | 88+ | ✅ | ✅ | Full support |
| Firefox | 79+ | ✅ | ✅ | Full support |
| Safari | 14+ | ✅ | ✅ | iOS 14.5+ |
| Edge | 88+ | ✅ | ✅ | Full support |

## Limitations and Considerations

### 1. Memory Requirements

- **Small models (100-200MB)**: 2GB+ RAM recommended
- **Medium models (300-500MB)**: 4GB+ RAM recommended
- **Large models (800MB+)**: 8GB+ RAM recommended

### 2. Storage Requirements

- Models are cached in IndexedDB
- Ensure sufficient storage space
- Implement cache management for multiple models

### 3. Network Considerations

- Initial model download requires internet
- After download, completely offline
- Consider offering different model sizes

## Example Use Cases

1. **AI-Powered Documentation**: Context-aware help systems
2. **Creative Writing Tools**: Story generation and editing assistance
3. **Code Assistants**: In-browser code completion and explanation
4. **Educational Platforms**: Interactive tutoring and Q&A
5. **Customer Support**: Intelligent chatbots with domain knowledge
6. **Content Analysis**: Text summarization and sentiment analysis

## Next Steps

1. **Try the demo**: [Live Web Demo](https://demo.runanywhere.ai/web)
2. **Check examples**: [GitHub Examples](https://github.com/RunanywhereAI/web-examples)
3. **Read documentation**: [Full Web SDK Docs](https://docs.runanywhere.ai/web)
4. **Join community**: [Discord](https://discord.gg/runanywhere)

The RunAnywhere Web SDK brings the power of on-device AI to the browser, enabling privacy-first,
offline-capable AI applications that work anywhere the web does!