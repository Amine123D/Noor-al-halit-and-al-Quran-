# Noor - ProGuard / R8 rules

# Keep the JavaScript bridge methods used by the WebView.
-keepclassmembers class com.amine.noor.MainActivity$NoorBridge {
    public *;
}

# Keep the bridge class itself.
-keep class com.amine.noor.MainActivity$NoorBridge {
    *;
}

# Keep JavaScript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
