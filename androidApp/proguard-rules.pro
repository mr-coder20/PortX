# PortX Professional Elite Obfuscation Rules

# 1. Standard Aggressive Obfuscation
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# 2. Keep Serialization Models (needed for functionality)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class kotlinx.serialization.json.** { *; }
-keep class com.mrcoder20.portx.data.network.** { *; }
-keep class com.mrcoder20.portx.domain.model.** { *; }

# 3. Koin Protection
-keep class org.koin.** { *; }
-keep class com.mrcoder20.portx.presentation.viewmodel.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# 4. Compose Stability
-keep class androidx.compose.** { *; }

# 5. Hide Domain Logic Strings
# Use -repackageclasses to flatten package structure (Anti-Analysis)
-repackageclasses 'com.mrcoder20.portx.internal'

# 7. Third-party Library Warnings (Ignore safe missing classes)
-dontwarn jakarta.servlet.**
-dontwarn ch.qos.logback.classic.servlet.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
