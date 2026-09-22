<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="LocalMind Android AI App" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Android Development" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose Material 3" />
  <img src="https://img.shields.io/badge/AI_Engine-llama.cpp-FF6F00?style=for-the-badge" alt="llama.cpp On-Device LLM" />
  <img src="https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen?style=for-the-badge" alt="Android 8.0 Oreo" />
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="MIT License" />
</p>

<p align="center">
  <img src="https://img.shields.io/github/v/release/tk85457/LocalMind?style=flat-square&color=brightgreen&label=Latest%20Release" alt="Latest Release" />
  <img src="https://img.shields.io/github/downloads/tk85457/LocalMind/total?style=flat-square&color=blue&label=Downloads" alt="Total Downloads" />
  <img src="https://img.shields.io/github/stars/tk85457/LocalMind?style=flat-square&color=yellow&label=Stars" alt="GitHub Stars" />
  <img src="https://img.shields.io/github/forks/tk85457/LocalMind?style=flat-square&color=orange&label=Forks" alt="Forks" />
  <img src="https://img.shields.io/github/last-commit/tk85457/LocalMind?style=flat-square&color=purple&label=Last%20Commit" alt="Last Commit" />
  <img src="https://img.shields.io/github/repo-size/tk85457/LocalMind?style=flat-square&color=red&label=Repo%20Size" alt="Repo Size" />
</p>

---

<p align="center">
  <img src="app_icon.png" width="200" alt="LocalMind App Icon" />
</p>

<h1 align="center">LocalMind â€” Private On-Device AI Assistant for Android</h1>

<p align="center">
  <strong>Run powerful Large Language Models (LLMs) entirely on your Android phone â€” no cloud servers, no API keys, no monthly subscriptions, no data collection. 100% offline, 100% private.</strong>
</p>

<p align="center">
  <em>The most advanced open-source on-device AI chatbot for Android. Powered by llama.cpp + GGUF models. Built with Kotlin, Jetpack Compose, and Material 3 design.</em>
</p>

<p align="center">
  <a href="https://github.com/tk85457/LocalMind/releases/latest"><strong>ðŸ“¥ Download Latest APK</strong></a> Â·
  <a href="#-getting-started"><strong>ðŸš€ Getting Started</strong></a> Â·
  <a href="#-features"><strong>âœ¨ Features</strong></a> Â·
  <a href="#-architecture"><strong>ðŸ—ï¸ Architecture</strong></a> Â·
  <a href="#-tech-stack"><strong>ðŸ› ï¸ Tech Stack</strong></a>
</p>

---

## ðŸ“– About LocalMind

**LocalMind** is a privacy-first, open-source Android application that brings the power of Large Language Models directly to your mobile device. Unlike cloud-based AI assistants like ChatGPT, Google Gemini, or Claude â€” LocalMind runs **100% on your phone** using the [llama.cpp](https://github.com/ggerganov/llama.cpp) inference engine with GGUF quantized models.

### Why LocalMind ?

| âŒ Cloud AI Problems | âœ… LocalMind Solution |
|---|---|
| Your data sent to remote servers | All data stays on your device |
| Requires internet connection | Works completely offline |
| Monthly subscription fees ($20/mo+) | Free and open-source forever |
| Rate limits and API quotas | Unlimited usage, no restrictions |
| Privacy concerns and data mining | Zero data collection, zero telemetry |
| Vendor lock-in | Open-source, own your AI stack |

---

## âœ¨ Features

### ðŸ”’ Privacy & Security
- **100% On-Device Inference** â€” Your conversations never leave your phone
- **Zero Network Requests** â€” No analytics, no tracking, no cloud dependencies
- **Biometric Authentication** â€” Lock LocalMind with fingerprint or face unlock
- **Local-First Storage** â€” All data stored in encrypted Room database

### ðŸ’¬ Intelligent Chat Interface
- **Material 3 Design** â€” Beautiful UI following Google's latest design language
- **Markdown Rendering** â€” Full markdown support with syntax highlighting via Markwon + Prism4j
- **Token-by-Token Streaming** â€” Real-time response generation for natural conversation flow
- **Chat History** â€” Persistent conversations with full search functionality
- **Collections** â€” Organize chats into custom folders and categories

### ðŸ¤– AI Model Management
- **Hugging Face Integration** â€” Browse, search, and download GGUF models from the world's largest model hub
- **Multiple Model Support** â€” Switch between different models (Llama, Mistral, Phi, Gemma, Qwen, etc.)
- **Background Downloads** â€” Download models via WorkManager with progress tracking
- **Model Cards** â€” View model metadata, parameters, and quantization details
- **Custom Prompt Templates** â€” Configure system prompts and chat templates per model

### ðŸ“ RAG â€” Document Chat (Retrieval-Augmented Generation)
- **PDF Upload & Parsing** â€” Extract text from PDF files via PdfBox Android
- **Context-Aware Responses** â€” Ask questions about your documents and get accurate answers
- **Local Document Processing** â€” No documents sent to any server, ever

### âš™ï¸ Advanced Inference Settings
- **Temperature** â€” Control response creativity (0.0 = deterministic â†’ 2.0 = creative)
- **Top-P (Nucleus Sampling)** â€” Fine-tune probability distributions
- **Top-K Sampling** â€” Limit vocabulary selection per token
- **Repeat Penalty** â€” Prevent repetitive outputs
- **Stop Words** â€” Custom stop sequences for response termination
- **BOS/EOS Tokens** â€” Granular control over generation boundaries
- **GPU Layer Offloading** â€” Maximize performance with hardware acceleration

### ðŸ“Š Performance Monitoring
- **Tokens/Second (t/s)** â€” Real-time throughput measurement
- **Time To First Token (TTFT)** â€” Latency tracking and optimization
- **GPU Layer Usage** â€” Monitor hardware utilization
- **Memory Footprint** â€” Track RAM consumption during inference

### ðŸŽ¨ Theming & Localization
- **Material You Dynamic Theming** â€” Automatic color extraction from wallpaper
- **Dark & Light Modes** â€” Full theme support with smooth transitions
- **Lottie Animations** â€” Premium micro-animations throughout the UI
- **Multi-Language Support** â€” Localized strings with full i18n/l10n framework

---

## ðŸ—ï¸ Architecture

LocalMind follows **Clean Architecture** with **MVVM** pattern and **Hilt dependency injection**, ensuring separation of concerns, testability, and maintainability.

```
com.tk854.localmind/
â”‚
â”œâ”€â”€ ðŸ“¦ core/                       # Foundation Layer
â”‚   â”œâ”€â”€ di/                        # Hilt modules (Database, Network, Engine)
â”‚   â”œâ”€â”€ engine/                    # LLM engine lifecycle & orchestration
â”‚   â”œâ”€â”€ performance/               # Benchmarking & performance profiling
â”‚   â”œâ”€â”€ rollout/                   # Feature flags & staged rollouts
â”‚   â”œâ”€â”€ storage/                   # File system & model storage manager
â”‚   â””â”€â”€ utils/                     # Shared utilities & extensions
â”‚
â”œâ”€â”€ ðŸ“Š data/                       # Data Layer
â”‚   â”œâ”€â”€ local/                     # Room DAOs, entities, type converters
â”‚   â”œâ”€â”€ mapper/                    # Entity â†” Domain model mappers
â”‚   â”œâ”€â”€ remote/                    # Hugging Face REST API (Retrofit)
â”‚   â””â”€â”€ repository/                # Repository implementations
â”‚
â”œâ”€â”€ ðŸ§© domain/                     # Domain Layer (Pure Kotlin)
â”‚   â”œâ”€â”€ model/                     # Domain models (Chat, Message, Model, Settings)
â”‚   â””â”€â”€ usecase/                   # Business logic use cases
â”‚
â”œâ”€â”€ ðŸ¤– llm/                        # LLM Integration Layer
â”‚   â”œâ”€â”€ native/                    # JNI bridge to llama.cpp (C++)
â”‚   â”œâ”€â”€ nativelib/                 # Native library loader & lifecycle
â”‚   â””â”€â”€ prompt/                    # Prompt template engine & chat formatters
â”‚
â”œâ”€â”€ ðŸ§­ navigation/                 # Navigation graph (Compose Navigation)
â”œâ”€â”€ ðŸ“¡ receiver/                   # Broadcast receivers
â”œâ”€â”€ âš™ï¸ service/                    # Background services (foreground inference)
â”‚
â”œâ”€â”€ ðŸŽ¨ ui/                         # Presentation Layer
â”‚   â”œâ”€â”€ components/                # Reusable Compose UI components
â”‚   â”œâ”€â”€ screens/                   # Screen composables (Chat, Models, Settings)
â”‚   â”œâ”€â”€ theme/                     # Material 3 theme, colors, typography
â”‚   â”œâ”€â”€ utils/                     # UI helpers & animation utilities
â”‚   â””â”€â”€ viewmodel/                 # ViewModels with StateFlow & SavedStateHandle
â”‚
â””â”€â”€ ðŸ‘· worker/                     # WorkManager tasks (model downloads)
```

### Native Layer (C++ / NDK)

```
app/src/main/cpp/
â”œâ”€â”€ CMakeLists.txt                 # CMake build configuration
â”œâ”€â”€ jni_bridge.cpp                 # JNI bridge: Kotlin â†” llama.cpp
â””â”€â”€ llama.cpp/                     # llama.cpp submodule (inference engine)
    â”œâ”€â”€ include/                   # Public headers (llama.h, ggml.h)
    â”œâ”€â”€ src/                       # Core source files
    â””â”€â”€ ggml/                      # GGML tensor library
```

---

## ðŸ› ï¸ Tech Stack

| Category | Technology | Purpose |
|----------|-----------|---------|
| **Language** | Kotlin 1.9.x | Primary development language |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative, modern UI |
| **Architecture** | Clean Architecture (MVVM) | Separation of concerns |
| **Dependency Injection** | Hilt (Dagger 2) | Compile-time DI |
| **Database** | Room + KSP | Local persistence |
| **Preferences** | DataStore | Key-value storage |
| **Networking** | Retrofit + OkHttp | Hugging Face API |
| **AI Engine** | llama.cpp (JNI/NDK) | On-device LLM inference |
| **Image Loading** | Coil Compose | Async image rendering |
| **Animations** | Lottie Compose | Premium micro-animations |
| **Markdown** | Markwon + Prism4j | Rich text rendering |
| **PDF Parsing** | PdfBox Android | Document text extraction |
| **Background Tasks** | WorkManager | Reliable task scheduling |
| **Build System** | Gradle (Kotlin DSL) + CMake | Multi-platform builds |
| **Native Build** | NDK 26 + CMake 3.22 | C++ compilation for ARM64 |

---

## ðŸš€ Getting Started

### Prerequisites

| Requirement | Version |
|------------|---------|
| Android Studio | Hedgehog (2023.1.1)+ |
| JDK | 17+ |
| Android SDK | API 34 |
| NDK | 26.1.10909125 |
| CMake | 3.22.1+ |

### Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/tk85457/LocalMind.git
cd LocalMind

# 2. Initialize llama.cpp submodule
git submodule update --init --recursive

# 3. Build debug APK
./gradlew assembleDebug

# 4. Install on connected device
./gradlew installDebug
```

### Pre-Built APK

Download the latest release APK from the [Releases](https://github.com/tk85457/LocalMind/releases/latest) page and install directly on your Android device.

> ðŸ“– See **[BUILD.md](BUILD.md)** for detailed build instructions, signing configuration, and troubleshooting.

---

## ðŸ“± Device Compatibility

| Spec | Requirement |
|------|------------|
| **Minimum Android** | 8.0 Oreo (API 26) |
| **Target Android** | 14 (API 34) |
| **Architecture** | arm64-v8a (64-bit ARM) |
| **RAM (Small Models)** | 4GB+ (1Bâ€“3B parameter models) |
| **RAM (Medium Models)** | 6GB+ (7B parameter models) |
| **RAM (Large Models)** | 8GB+ (13B+ parameter models) |
| **Storage** | 2â€“10GB per model (varies by quantization) |

### Supported Model Formats

- **GGUF** quantized models (Q2_K, Q3_K_S, Q4_0, Q4_K_M, Q5_K_M, Q8_0, F16)
- Compatible families: **Llama 3**, **Mistral**, **Phi-3**, **Gemma**, **Qwen 2**, **Yi**, **StableLM**, and more

---

## ðŸ—ºï¸ Roadmap

- [ ] ðŸ–¼ï¸ Multimodal support (image + text models like LLaVA)
- [ ] ðŸŽ™ï¸ Voice input with on-device speech-to-text
- [ ] ðŸ“¤ Chat export (JSON, Markdown, PDF)
- [ ] ðŸ”Œ Plugin system for custom tools
- [ ] ðŸŒ Web UI companion app
- [ ] ðŸ“Š Advanced RAG with vector embeddings
- [ ] âŒš Wear OS companion
- [ ] ðŸ–¥ï¸ Desktop version (Windows/macOS/Linux)

---

## ðŸ¤ Contributing

Contributions are welcome! Whether it's bug fixes, new features, documentation, or translations â€” all contributions are appreciated.

1. **Fork** the repository
2. **Create** your feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to the branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request

---

## ðŸ“„ License

This project is licensed under the **MIT License** â€” see the [LICENSE](LICENSE) file for details.

---

## ðŸ™ Acknowledgments

| Project | Contribution |
|---------|-------------|
| [llama.cpp](https://github.com/ggerganov/llama.cpp) | C/C++ LLM inference engine |
| [PocketPal AI](https://github.com/a-ghorbani/pocketpal-ai) | Behavioral patterns for model management |
| [Hugging Face](https://huggingface.co/) | Model hosting and discovery platform |
| [Markwon](https://github.com/noties/Markwon) | Android Markdown rendering |
| [Lottie](https://github.com/airbnb/lottie-android) | Animation framework |

---

## â­ Star History

If you find LocalMind useful, please consider giving it a â­ â€” it helps others discover the project!

---

<p align="center">
  <strong>ðŸ§  LocalMind â€” Your AI. Your Data. Your Device.</strong>
</p>
<p align="center">
  Built with â¤ï¸ for privacy-conscious AI enthusiasts
</p>
<p align="center">
  <a href="https://github.com/tk85457/LocalMind/releases/latest">ðŸ“¥ Download</a> Â·
  <a href="https://github.com/tk85457/LocalMind/issues">ðŸ› Report Bug</a> Â·
  <a href="https://github.com/tk85457/LocalMind/issues">ðŸ’¡ Request Feature</a>
</p>

---

<!-- SEO Keywords: Android AI App, On-Device LLM, Offline AI Chat, Private AI Assistant, llama.cpp Android, GGUF Models Android, Local AI Chatbot, Open Source AI Android, Run LLM on Phone, Kotlin Jetpack Compose AI, Material 3 AI Chat, Hugging Face Android, Privacy First AI, No Cloud AI, Free AI App Android, RAG Android, Document Chat AI, On-Device Machine Learning, Edge AI Android, LLM Inference Mobile -->
