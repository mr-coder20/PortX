# Changelog

## [5.0.0-ULTRA] - 2026-06-21

### 🚀 Massive Performance Upgrade (The "Ultra Engine")
- **Decoupled Scanning Engine**: Refactored the core logic to separate port discovery from service detection. Scanning speed is no longer limited by slow banner-grabbing responses.
- **Adaptive RTT Layer**: Introduced dynamic timeout calculation based on real-time network latency (RTT).
- **Neural-Inspired Timing**: Added `AdaptiveTiming` with "Turbo Mode" exploration for higher throughput on stable networks.
- **Bounded Channels**: Implemented backpressure using Kotlin Channels to ensure memory stability during full 65k port scans.

### 🎨 UI & Branding
- **Modern Iconography**: Integrated new professional high-tech icons across all platforms (Android, Windows, macOS, Linux).
- **Compose Resources Migration**: Migrated to the modern `org.jetbrains.compose.resources` library for centralized resource management.

### 🔧 Fixes & Refinement
- **DNS Caching**: Target hostnames are now resolved once per scan.
- **Accuracy Retries**: Added automatic retries for "filtered" ports in high-performance modes.
- **Warning Clean-up**: Resolved numerous compiler and deprecation warnings across the codebase.

---

## [4.0.0] - Previous Version
- Initial implementation of the KMP Port Scanner.
- Basic TCP/UDP scanning support.
- Initial Compose Multiplatform UI.
