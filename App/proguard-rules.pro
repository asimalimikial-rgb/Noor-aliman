# Preserve JavaScript bridge entry points exposed to the WebView.
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class app.nooraliman.MainActivity$AlarmBridge { *; }
-keep class app.nooraliman.MainActivity$TafsirBridge { *; }
-keep class app.nooraliman.CompassBridge { *; }

# Android instantiates these components from the manifest.
-keep class app.nooraliman.MainActivity { *; }
-keep class app.nooraliman.AdhanAlertActivity { *; }
-keep class app.nooraliman.** extends android.content.BroadcastReceiver { *; }
-keep class app.nooraliman.** extends android.app.Service { *; }
