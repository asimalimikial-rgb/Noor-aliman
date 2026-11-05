package app.nooraliman;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Insets;
import android.util.Log;
import android.view.WindowInsets;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST = 41;
    private static final int NOTIFICATION_REQUEST = 42;
    private static final int CAMERA_REQUEST = 43;
    private WebView webView;
    private GeolocationPermissions.Callback locationCallback;
    private String locationOrigin;
    private CompassBridge compassBridge;
    private PermissionRequest cameraPermissionRequest;
    private OnBackInvokedCallback backInvokedCallback;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF102C26);
        getWindow().setNavigationBarColor(0xFF102C26);
        if (Build.VERSION.SDK_INT >= 29) getWindow().setNavigationBarContrastEnforced(false);
        if (Build.VERSION.SDK_INT >= 35) {
            // Android 15+ enforces edge-to-edge for apps targeting API 35 and above.
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        }

        webView = new WebView(this);
        webView.setOnApplyWindowInsetsListener((view, insets) -> {
            int left, top, right, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout());
                left = bars.left; top = bars.top; right = bars.right; bottom = bars.bottom;
            } else {
                left = insets.getSystemWindowInsetLeft();
                top = insets.getSystemWindowInsetTop();
                right = insets.getSystemWindowInsetRight();
                bottom = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(left, top, right, bottom);
            return insets;
        });
        setContentView(webView);
        compassBridge = new CompassBridge(this);
        if (Build.VERSION.SDK_INT >= 33) {
            backInvokedCallback = this::handleBackNavigation;
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, backInvokedCallback);
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMediaPlaybackRequiresUserGesture(true);

        webView.addJavascriptInterface(new AlarmBridge(), "NativeAlarms");
        webView.addJavascriptInterface(compassBridge, "NativeCompass");
        webView.addJavascriptInterface(new TafsirBridge(), "NativeTafsir");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (isTrustedAppOrigin(uri)) return false;
                String scheme = uri.getScheme();
                if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (Exception ignored) { }
                }
                // Block file:, content:, javascript:, intent: and unknown schemes.
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                if (message != null && message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    Log.e("NoorAlImanWeb", message.message() + " (" + message.sourceId()
                            + ":" + message.lineNumber() + ")");
                }
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(
                    String origin, GeolocationPermissions.Callback callback) {
                Uri requestedOrigin = Uri.parse(origin == null ? "" : origin);
                if (!isTrustedAppOrigin(requestedOrigin)) {
                    callback.invoke(origin, false, false);
                    return;
                }
                if (hasLocationPermission()) {
                    callback.invoke(origin, true, false);
                    return;
                }
                locationOrigin = origin;
                locationCallback = callback;
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_REQUEST);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                Uri origin = request.getOrigin();
                if (!isTrustedAppOrigin(origin)) {
                    request.deny();
                    return;
                }
                boolean videoRequested = false;
                for (String resource : request.getResources()) {
                    if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
                        videoRequested = true;
                    } else {
                        request.deny();
                        return;
                    }
                }
                if (!videoRequested) {
                    request.deny();
                    return;
                }
                if (hasCameraPermission()) {
                    request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                    return;
                }
                if (cameraPermissionRequest != null) cameraPermissionRequest.deny();
                cameraPermissionRequest = request;
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
            }

            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                if (cameraPermissionRequest == request) cameraPermissionRequest = null;
            }
        });

        loadApplicationAssets();
    }

    private static boolean isTrustedAppOrigin(Uri uri) {
        return uri != null && "https".equalsIgnoreCase(uri.getScheme())
                && "appassets.androidplatform.net".equalsIgnoreCase(uri.getHost())
                && (uri.getPort() == -1 || uri.getPort() == 443);
    }

    private void loadApplicationAssets() {
        try {
            String html = readAsset("noor-aliman.html");
            String enhancements = readAsset("enhancements.js");
            int bodyEnd = html.toLowerCase(java.util.Locale.ROOT).lastIndexOf("</body>");
            if (bodyEnd < 0) throw new IllegalStateException("Missing HTML body");
            html = html.substring(0, bodyEnd)
                    + "\n<script>\n" + enhancements + "\n</script>\n"
                    + html.substring(bodyEnd);
            webView.loadDataWithBaseURL("https://appassets.androidplatform.net/", html,
                    "text/html", "UTF-8", null);
        } catch (Exception error) {
            webView.loadDataWithBaseURL("https://appassets.androidplatform.net/",
                    "<meta charset='utf-8'><p dir='rtl'>تعذّر فتح التطبيق.</p>",
                    "text/html", "UTF-8", null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    private void handleBackNavigation() {
        if (webView == null) { finish(); return; }
        if (webView.canGoBack()) { webView.goBack(); return; }
        webView.evaluateJavascript(
                "window.handleAndroidBack ? window.handleAndroidBack() : false", value -> {
                    if (!"true".equals(value)) finish();
                });
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }

    private String readAsset(String name) throws Exception {
        try (InputStream input = getAssets().open(name);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    public final class TafsirBridge {
        @JavascriptInterface
        public String loadSaadiSura(int surah) {
            if (surah < 1 || surah > 114) return "[]";
            try {
                return readAsset("tafsir-saadi/"
                        + String.format(java.util.Locale.ROOT, "%03d", surah) + ".json");
            } catch (Exception ignored) {
                return "[]";
            }
        }
    }

    private static boolean oneOf(String value, String... allowed) {
        if (value == null) return false;
        for (String candidate : allowed) if (candidate.equals(value)) return true;
        return false;
    }

    private static boolean isValidTimezone(String value) {
        if (value == null || value.length() > 64) return false;
        for (String id : java.util.TimeZone.getAvailableIDs()) if (id.equals(value)) return true;
        return false;
    }

    private static boolean isValidClock(String value) {
        if (value == null || !value.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) return false;
        return true;
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraPermission() {
        return checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean alertsEnabled() {
        return getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).getBoolean("adhan", false)
                || getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).getBoolean("reminder", false)
                || getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).getBoolean("dhikrOn", false);
    }

    private boolean hasNotificationPermission() {
        return Build.VERSION.SDK_INT < 33
                || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasExactAlarmPermission() {
        return Build.VERSION.SDK_INT < 31
                || ((AlarmManager) getSystemService(ALARM_SERVICE)).canScheduleExactAlarms();
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= 31 && !hasExactAlarmPermission()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName()));
            try {
                startActivity(intent);
            } catch (Exception ignored) {
            }
            return;
        }
        PrayerAlarmScheduler.schedule(this);
        DhikrAlarmScheduler.schedule(this);
    }

    private void syncAlarmSchedule() {
        if (!alertsEnabled()) {
            PrayerAlarmScheduler.cancel(this);
            DhikrAlarmScheduler.cancel(this);
            stopService(new Intent(this, AdhanPlaybackService.class));
            stopService(new Intent(this, DhikrPlaybackService.class));
            return;
        }
        android.content.SharedPreferences prefs = getSharedPreferences(
                PrayerAlarmScheduler.PREFS, MODE_PRIVATE);
        boolean notificationsNeeded = prefs.getBoolean("reminder", false)
                || prefs.getBoolean("dhikrOn", false)
                || (prefs.getBoolean("adhan", false)
                && prefs.getBoolean("showAdhanNotification", true));
        if (Build.VERSION.SDK_INT >= 33 && notificationsNeeded && !hasNotificationPermission()) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_REQUEST);
            return;
        }
        requestExactAlarmPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alertsEnabled()) {
            // Prayer scheduler selects exact or inexact delivery based on existing system access.
            PrayerAlarmScheduler.schedule(this);
            if (hasExactAlarmPermission()) DhikrAlarmScheduler.schedule(this);
        }
        if (webView != null) {
            webView.post(() -> webView.evaluateJavascript(
                    "if(window.refreshQiblaCompass)window.refreshQiblaCompass()", null));
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.evaluateJavascript("if(window.stopQiblaCamera)window.stopQiblaCamera()", null);
        }
        if (compassBridge != null) compassBridge.stop();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST && locationCallback != null) {
            locationCallback.invoke(locationOrigin, hasLocationPermission(), false);
            locationCallback = null;
            locationOrigin = null;
        } else if (requestCode == NOTIFICATION_REQUEST) {
            requestExactAlarmPermission();
        } else if (requestCode == CAMERA_REQUEST) {
            PermissionRequest request = cameraPermissionRequest;
            cameraPermissionRequest = null;
            if (request != null) {
                if (hasCameraPermission()) {
                    request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                } else {
                    request.deny();
                }
            }
        }
    }

    public final class AlarmBridge {
        @JavascriptInterface
        public void reportWebError(String kind, String message) {
            if (kind == null || message == null) return;
            Log.e("NoorAlImanWeb", kind.substring(0, Math.min(kind.length(), 40))
                    + ": " + message.substring(0, Math.min(message.length(), 700)));
        }

        @JavascriptInterface
        public String loadWebSettings() {
            try {
                return new org.json.JSONObject(getSharedPreferences(
                        "noor_web_settings", MODE_PRIVATE).getAll()).toString();
            } catch (Exception ignored) {
                return "{}";
            }
        }

        @JavascriptInterface
        public void saveWebSetting(String key, String jsonValue) {
            if (key == null || !key.matches("[A-Za-z0-9_-]{1,80}")
                    || jsonValue == null || jsonValue.length() > 16_384) return;
            try {
                new org.json.JSONTokener(jsonValue).nextValue();
                getSharedPreferences("noor_web_settings", MODE_PRIVATE)
                        .edit().putString(key, jsonValue).apply();
            } catch (Exception ignored) {
                // Ignore malformed preference values; never clear existing settings.
            }
        }

        @JavascriptInterface
        public void configure(double lat, double lng, String timezone, String method,
                              String asr, boolean adhan, boolean reminder, String adhanSound) {
            if (Double.isNaN(lat) || Double.isInfinite(lat) || lat < -90 || lat > 90
                    || Double.isNaN(lng) || Double.isInfinite(lng) || lng < -180 || lng > 180
                    || !isValidTimezone(timezone)
                    || !oneOf(method, "mwl", "umm", "egypt", "karachi")
                    || !oneOf(asr, "std", "hanafi")
                    || !oneOf(adhanSound, "classic", "aqib", "short", "azemmour", "makkah", "doha")) return;
            getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                    .putFloat("latitude", (float) lat)
                    .putFloat("longitude", (float) lng)
                    .putString("timezone", timezone)
                    .putString("method", method)
                    .putString("asr", asr)
                    .putBoolean("adhan", adhan)
                    .putBoolean("reminder", reminder)
                    .putString("adhanSound", adhanSound)
                    .apply();
            runOnUiThread(() -> syncAlarmSchedule());
        }

        @JavascriptInterface
        public void preview(String sound) {
            if (!oneOf(sound, "classic", "aqib", "short", "azemmour", "makkah", "doha")) return;
            runOnUiThread(() -> {
                Intent service = new Intent(MainActivity.this, AdhanPlaybackService.class)
                        .setAction(AdhanPlaybackService.ACTION_PLAY)
                        .putExtra(PrayerAlarmScheduler.EXTRA_SOUND, sound)
                        .putExtra(PrayerAlarmScheduler.EXTRA_PREVIEW, true);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(service);
                else startService(service);
            });
        }

        @JavascriptInterface
        public void stopPreview() {
            runOnUiThread(() -> startService(new Intent(MainActivity.this,
                    AdhanPlaybackService.class).setAction(AdhanPlaybackService.ACTION_STOP_PREVIEW)));
        }

        @JavascriptInterface
        public boolean isAdhanPlaying() {
            return AdhanPlaybackService.isPlaying();
        }

        @JavascriptInterface
        public void stopAdhan() {
            runOnUiThread(() -> startService(new Intent(MainActivity.this,
                    AdhanPlaybackService.class).setAction(AdhanPlaybackService.ACTION_STOP)));
        }

        @JavascriptInterface
        public void configurePlaybackOptions(boolean showAdhanNotification) {
            getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                    .putBoolean("showAdhanNotification", showAdhanNotification)
                    .apply();
            runOnUiThread(() -> startService(new Intent(MainActivity.this,
                    AdhanPlaybackService.class)
                    .setAction(AdhanPlaybackService.ACTION_UPDATE_NOTIFICATION)));
        }

        @JavascriptInterface
        public boolean canUseFullScreenIntent() {
            return Build.VERSION.SDK_INT < 34
                    || ((android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .canUseFullScreenIntent();
        }

        @JavascriptInterface
        public void requestFullScreenIntentAccess() {
            if (Build.VERSION.SDK_INT < 34 || canUseFullScreenIntent()) return;
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception ignored) { }
        }

        @JavascriptInterface
        public void configureFaceDownStop(boolean enabled) {
            getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                    .putBoolean("stopAdhanFaceDown", enabled).apply();
            if (AdhanPlaybackService.isPlaybackActive()) {
                runOnUiThread(() -> startService(new Intent(MainActivity.this,
                        AdhanPlaybackService.class)
                        .setAction(AdhanPlaybackService.ACTION_UPDATE_SENSOR_SETTINGS)));
            }
        }

        @JavascriptInterface
        public void configureVolumeKeyStop(boolean enabled) {
            getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                    .putBoolean("stopAdhanVolumeKeys", enabled).apply();
        }

        @JavascriptInterface
        public void configureScreenOffStop(boolean enabled) {
            getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                    .putBoolean("stopAdhanScreenOff", enabled).apply();
            if (AdhanPlaybackService.isPlaybackActive()) {
                runOnUiThread(() -> startService(new Intent(MainActivity.this,
                        AdhanPlaybackService.class)
                        .setAction(AdhanPlaybackService.ACTION_UPDATE_NOTIFICATION)));
            }
        }

        @JavascriptInterface
        public void configurePrayerAdjustments(String offsets) {
            if (offsets == null || offsets.length() > 2048) return;
            String safeOffsets = "{}";
            try {
                org.json.JSONObject parsed = new org.json.JSONObject(offsets);
                for (String key : new String[]{"fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"}) {
                    parsed.put(key, Math.max(-180, Math.min(180, parsed.optInt(key, 0))));
                }
                safeOffsets = parsed.toString();
            } catch (Exception ignored) {
            }
            getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                    .putString("prayerAdjustments", safeOffsets)
                    .apply();
        }

        @JavascriptInterface
        public void previewSalawat(double volume) {
            if (Double.isNaN(volume) || Double.isInfinite(volume)) return;
            runOnUiThread(() -> {
                Intent service = new Intent(MainActivity.this, DhikrPlaybackService.class)
                        .setAction(DhikrPlaybackService.ACTION_PLAY)
                        .putExtra(DhikrAlarmScheduler.EXTRA_TYPE, "salawat")
                        .putExtra(DhikrAlarmScheduler.EXTRA_VOLUME,
                                (float) Math.max(0d, Math.min(1d, volume)));
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(service);
                else startService(service);
            });
        }

        @JavascriptInterface
        public void configureDhikr(boolean enabled, int every, String type, double volume,
                                   String from, String to) {
            if (!oneOf(type, "subhan", "hamd", "akbar", "salawat")
                    || Double.isNaN(volume) || Double.isInfinite(volume)
                    || !isValidClock(from) || !isValidClock(to)) return;
            int safeEvery = Math.max(5, Math.min(120, every));
            float safeVolume = (float) Math.max(0d, Math.min(1d, volume));
            getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                    .putBoolean("dhikrOn", enabled)
                    .putInt("dhikrEvery", safeEvery)
                    .putString("dhikrType", type)
                    .putFloat("dhikrVol", safeVolume)
                    .putString("dhikrFrom", from)
                    .putString("dhikrTo", to)
                    .apply();
            runOnUiThread(() -> {
                if (enabled) syncAlarmSchedule();
                else {
                    DhikrAlarmScheduler.cancel(MainActivity.this);
                    stopService(new Intent(MainActivity.this, DhikrPlaybackService.class));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (cameraPermissionRequest != null) {
            cameraPermissionRequest.deny();
            cameraPermissionRequest = null;
        }
        if (compassBridge != null) compassBridge.stop();
        if (Build.VERSION.SDK_INT >= 33 && backInvokedCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backInvokedCallback);
            backInvokedCallback = null;
        }
        if (webView != null) {
            webView.removeJavascriptInterface("NativeAlarms");
            webView.removeJavascriptInterface("NativeCompass");
            webView.removeJavascriptInterface("NativeTafsir");
            webView.destroy();
        }
        super.onDestroy();
    }
}
