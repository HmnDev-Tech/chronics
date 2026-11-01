# Chronika keeps Shizuku reflection access working in release builds.
-keep class rikka.shizuku.** { *; }
-keepclassmembers class rikka.shizuku.Shizuku {
    private static *** newProcess(...);
}
-dontwarn rikka.shizuku.**

# Native Shevery Privilege API
-keep class com.hamondev.shevery.api.** { *; }
-dontwarn com.hamondev.shevery.api.**
