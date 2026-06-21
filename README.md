# PortX: Ultra-Fast Network Port Scanner (KMP)
## 🚀 Powered by the "Ultra Engine" v5.0.0

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-orange.svg?style=flat)](https://github.com/JetBrains/compose-multiplatform)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Windows%20%7C%20macOS%20%7C%20Linux-green.svg?style=flat)](#)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

### [فارسی] معرفی پروژه
**PortX** یک اسکنر پورت شبکه فوق سریع، امن و چند پلتفرمی است که با استفاده از **Kotlin Multiplatform** توسعه یافته است. این پروژه با بهره‌گیری از موتور اختصاصی **Ultra Engine** و معماری مبتنی بر **Coroutines Channels**، استانداردهای جدیدی را در سرعت و امنیت اسکن شبکه تعریف می‌کند. این ابزار نه تنها برای سرعت (۵۰,۰۰۰ پورت در ثانیه)، بلکه برای **امنیت و مقاومت در برابر تحلیل معکوس** مهندسی شده است.

### [English] Project Overview
**PortX** is an ultra-fast, production-grade, and secure network port scanner built with **Kotlin Multiplatform (KMP)**. Powered by the custom **Ultra Engine v5**, it redefines network exploration with neural-inspired adaptive timing and extreme throughput. Beyond speed, it is architected with **advanced security hardening and obfuscation**, making it a robust tool for security professionals.

---

## 💎 Elite Features | قابلیت‌های کلیدی

| Feature | Technical Excellence | توضیحات فنی |
| :--- | :--- | :--- |
| **Ultra Engine v5** | Neural-inspired adaptive scanning logic. | موتور هوشمند با منطق تطبیقی عصبی. |
| **Adaptive RTT** | Moving average timeout adjustment (2.5x RTT). | تنظیم هوشمند تایم‌اوت بر اساس تاخیر لحظه‌ای. |
| **Turbo Mode** | High-throughput bursting (50,000+ PPS). | اسکن با نرخ بسیار بالا (بیش از ۵۰ هزار پورت). |
| **Security Hardened** | Aggressive R8/ProGuard obfuscation & Anti-RE. | امنیت بالا و مقاوم‌سازی در برابر مهندسی معکوس. |
| **Decoupled Pool** | Zero-blocking scanner vs banner worker logic. | جداسازی کامل بخش اسکن از تشخیص سرویس. |
| **Memory Safety** | Bounded Channels preventing OOM during 65k scans. | مدیریت امن حافظه در اسکن‌های عظیم. |
| **Service Detection** | Advanced Banner Grabbing & HTTP Fingerprinting. | تشخیص هوشمند سرویس‌ها و عنوان صفحات وب. |

---

## 🛠 Technical Architecture & Security | معماری فنی و امنیت

### 1. Neural-Inspired Adaptive Timing (تایمینگ هوشمند)
**[EN]** Using a 2.5x moving average of **Round Trip Time (RTT)**, the engine dynamically adjusts timeouts. This ensures maximum speed on local networks and maximum accuracy on high-latency mobile networks (LTE/5G).  
**[FA]** با استفاده از میانگین متحرک ۲.۵ برابری **RTT**، تایم‌اوت‌ها به صورت لحظه‌ای تنظیم می‌شوند؛ تضمین سرعت خیره‌کننده در شبکه محلی و دقت ۱۰۰٪ در شبکه‌های موبایل.

### 2. Security Hardening (امنیت و مقاوم‌سازی)
**[EN]** PortX is built for professional use. It includes:
- **Repackaging Protection**: Internal logic is moved to obfuscated packages.
- **Aggressive Minification**: Reduced binary footprint while hiding domain logic.
- **ProGuard/R8 Hardening**: Custom rules to protect Koin DI and Serialization models.
**[FA]** این پروژه با استانداردهای امنیتی بالا ساخته شده است:
- **مقاومت در برابر تحلیل**: انتقال منطق برنامه به پکیج‌های مبهم‌سازی شده.
- **بهینه‌سازی حداکثری**: کاهش حجم فایل خروجی همزمان با مخفی‌سازی کدهای حساس.

### 3. Modern KMP Stack (تکنولوژی‌های مدرن)
- **UI**: Compose Multiplatform (Material 3) - Shared UI logic across all platforms.
- **Networking**: Ktor Sockets (Low-level TCP/UDP interaction).
- **Concurrency**: Kotlin Coroutines (Structured Concurrency with Channels & Flows).
- **DI**: Koin (Multiplatform Dependency Injection).
- **Storage**: SQLDelight (Typesafe local persistence).

---

## 🚀 Native Distribution | خروجی‌های بومی
PortX compiles to **true native binaries** for each platform, ensuring maximum performance:
- **Android**: Signed APK with Adaptive Icons.
- **Windows**: Professional `.msi` installers.
- **macOS/Linux**: Native `.dmg` and `.deb` support via KMP.

---

## 📸 Banner & Branding | برندینگ
> [!TIP]
> **PortX** uses a professional, high-tech icon system. The UI is designed with Material 3 to provide a premium, modern experience on every OS.

---

## 📄 License & Credits
This project is licensed under the MIT License.
Developed with ❤️ by **[mr-coder20](https://github.com/mr-coder20)**
