# PortX Desktop Professional Obfuscation Rules

# 1. Standard Aggressive Obfuscation
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# 2. Keep Serialization Models
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class kotlinx.serialization.json.** { *; }
-keep class com.mrcoder20.portx.data.network.** { *; }

# 3. Koin Protection
-keep class org.koin.** { *; }

# 4. Compose Desktop Specifics
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.skiko.** { *; }
-keep class org.jetbrains.skia.** { *; }

# 5. Third-party Library Warnings (Ignore safe missing classes)
-dontwarn jakarta.servlet.**
-dontwarn ch.qos.logback.classic.servlet.**
-dontwarn ch.qos.logback.classic.helpers.MDCInsertingServletFilter
-dontwarn ch.qos.logback.classic.selector.servlet.**
-dontwarn ch.qos.logback.core.status.ViewStatusMessagesServletBase
-dontwarn ch.qos.logback.core.net.LoginAuthenticator
-dontwarn ch.qos.logback.core.net.SMTPAppenderBase
-dontwarn ch.qos.logback.core.rolling.helper.XZCompressionStrategy
-dontwarn jakarta.mail.**
-dontwarn org.codehaus.janino.**
-dontwarn org.codehaus.commons.compiler.**
-dontwarn org.tukaani.xz.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# 6. Compose Resources
-keep class com.mrcoder20.portx.shared.Res { *; }
-keep class com.mrcoder20.portx.shared.Res$* { *; }
