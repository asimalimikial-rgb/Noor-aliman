package app.nooraliman;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
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

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF102C26);
        getWindow().setNavigationBarColor(0xFF102C26);

        webView = new WebView(this);
        setContentView(webView);
        compassBridge = new CompassBridge(this);

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
                if (uri.getHost() == null || uri.getHost().equals("appassets.androidplatform.net")) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) {
                }
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(
                    String origin, GeolocationPermissions.Callback callback) {
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
                if (!"https".equals(origin.getScheme())
                        || !"appassets.androidplatform.net".equals(origin.getHost())) {
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

        try {
            String html = readAsset("noor-aliman.html");
            String enhancements = readAsset("enhancements.js");
            int bodyEnd = html.toLowerCase(java.util.Locale.ROOT).lastIndexOf("</body>");
            if (bodyEnd < 0) throw new IllegalStateException("Missing HTML body");
            html = html.substring(0, bodyEnd)
                    + "\n<script>\n" + enhancements + "\n</script>\n"
                    + html.substring(bodyEnd);
            webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    html,
                    "text/html",
                    "UTF-8",
                    null);
        } catch (Exception error) {
            webView.loadDataWithBaseURL(null,
                    "<meta charset='utf-8'><p dir='rtl'>تعذّر فتح التطبيق.</p>",
                    "text/html", "UTF-8", null);
        }
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
        if (alertsEnabled() && hasExactAlarmPermission()) {
            PrayerAlarmScheduler.schedule(this);
            DhikrAlarmScheduler.schedule(this);
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
        public void configure(double lat, double lng, String timezone, String method,
                              String asr, boolean adhan, boolean reminder, String adhanSound) {
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
        public void configurePrayerAdjustments(String offsets) {
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
            getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                    .putBoolean("dhikrOn", enabled)
                    .putInt("dhikrEvery", every)
                    .putString("dhikrType", type)
                    .putFloat("dhikrVol", (float) volume)
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
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
