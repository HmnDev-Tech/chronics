# Chronika keeps Shizuku reflection access working in release builds.
-keep class rikka.shizuku.** { *; }
-keepclassmembers class rikka.shizuku.Shizuku {
    private static *** newProcess(...);
}
-dontwarn rikka.shizuku.**
