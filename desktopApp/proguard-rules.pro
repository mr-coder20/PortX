# PortX Desktop Minimalist Safety Rules
# Disabling aggressive obfuscation to ensure JVM stability on Windows

-dontobfuscate
-dontoptimize
-dontshrink

# Keep everything to be 100% safe
-keep class com.mrcoder20.portx.** { *; }
-keep class ** { *; }

# Ignore all warnings to prevent build failures
-dontwarn **
