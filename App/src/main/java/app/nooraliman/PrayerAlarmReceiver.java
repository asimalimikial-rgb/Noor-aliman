package app.nooraliman;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    private static final String REMINDER_CHANNEL = "prayer_reminders_v2";
    private static final String ADHAN_FAILURE_CHANNEL = "adhan_failure_v2";
    private static final String TAG = "NoorPrayerAlarm";
    private static final long MINUTE = 60_000L;
    // Adhan is time-critical: never play it more than five minutes after prayer time.
    private static final long ADHAN_LATE_GRACE_MS = 5L * MINUTE;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        if (PrayerAlarmScheduler.ACTION_REFRESH.equals(intent.getAction())) {
            PrayerAlarmScheduler.schedule(context, true);
            DhikrAlarmScheduler.schedule(context);
            return;
        }
        if (DhikrAlarmScheduler.ACTION_FIRE.equals(intent.getAction())) {
            DhikrAlarmScheduler.fire(context);
            return;
        }
        if (!PrayerAlarmScheduler.ACTION_FIRE.equals(intent.getAction())) return;
        try {
            String type = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TYPE);
            String key = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_KEY);
            String name = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_NAME);
            long expectedAt = expectedAlarmAt(intent);
            long prayerAt = intent.getLongExtra(PrayerAlarmScheduler.EXTRA_PRAYER_AT,
                    "reminder".equals(type) ? expectedAt + 10L * MINUTE : expectedAt);
            recordDelivery(context, intent, type, key, prayerAt);
            SharedPreferences prefs = AlarmPrefs.get(context);
            boolean wasExact = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_EXACT, true);
            AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            boolean exactStillAllowed = Build.VERSION.SDK_INT < 31
                    || (alarms != null && alarms.canScheduleExactAlarms());
            if (Build.VERSION.SDK_INT >= 31 && (!wasExact || !exactStillAllowed)) {
                Log.w(TAG, "Prayer alert fired via inexact fallback: " + type + "/" + key);
                prefs.edit().putString("lastAlarmDroppedReason",
                        "inexact_fallback_used").apply();
            }
            if ("adhan".equals(type) && prefs.getBoolean("adhan", false)
                    && !shouldStartAdhan(wasExact, exactStillAllowed, Build.VERSION.SDK_INT)) {
                String detail = "Adhan audio was blocked because the alarm was not exact or exact-alarm access was revoked";
                recordPlaybackFailure(context, "exact-alarm-required", detail, key);
                prefs.edit().putString("lastAlarmDroppedReason",
                        "adhan_requires_exact_alarm").apply();
                showAdhanFailure(context, "exact-alarm-required", name, key,
                        "لم يبدأ الأذان تلقائياً لأن التنبيه لم يكن دقيقاً. فعّل إذن «التنبيهات الدقيقة» من إعدادات التطبيق.");
                return;
            }
            if ("adhan".equals(type) && prefs.getBoolean("adhan", false)) {
                long receivedAt = System.currentTimeMillis();
                if (isAdhanTooLate(expectedAt, receivedAt)) {
                    String detail = "Alarm arrived " + (receivedAt - expectedAt)
                            + "ms after prayer time; stale adhan skipped";
                    recordPlaybackFailure(context, "late-alarm-dropped", detail, key);
                    prefs.edit().putString("lastAlarmDroppedReason", "adhan_late_over_5_minutes").apply();
                    Log.w(TAG, detail + "; prayer=" + key);
                    showAdhanFailure(context, "late-alarm-dropped", name, key,
                            "وصل تنبيه الأذان بعد الموعد بأكثر من خمس دقائق؛ تم تجاهله.");
                    return;
                }
                Intent service = new Intent(context, AdhanPlaybackService.class)
                        .setAction(AdhanPlaybackService.ACTION_PLAY)
                        .putExtra(PrayerAlarmScheduler.EXTRA_NAME, name)
                        .putExtra(PrayerAlarmScheduler.EXTRA_KEY, key)
                        .putExtra(PrayerAlarmScheduler.EXTRA_SOUND,
                                prefs.getString("adhanSound", "doha"));
                try {
                    if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
                    else context.startService(service);
                } catch (RuntimeException startFailure) {
                    String exceptionType = startFailure.getClass().getSimpleName();
                    boolean backgroundStartBlocked = Build.VERSION.SDK_INT >= 31
                            && "ForegroundServiceStartNotAllowedException".equals(exceptionType);
                    String failureState = backgroundStartBlocked
                            ? "foreground-service-start-not-allowed"
                            : "foreground-service-start-failed";
                    Log.e(TAG, "Could not start adhan playback service for " + key
                            + "; exception=" + exceptionType, startFailure);
                    recordPlaybackFailure(context, failureState, startFailure.toString(), key);
                    showAdhanStartFailure(context, name, key, backgroundStartBlocked);
                }
            } else if ("reminder".equals(type) && prefs.getBoolean("reminder", false)) {
                int minutesRemaining = remainingReminderMinutes(prayerAt, System.currentTimeMillis());
                if (minutesRemaining <= 0) {
                    prefs.edit().putString("lastAlarmDroppedReason", "reminder_after_prayer_time").apply();
                    Log.i(TAG, "Dropped stale reminder after prayer time: " + key);
                    return;
                }
                showReminder(context, key, name, intent.getDataString(), minutesRemaining);
            }
        } finally {
            try { PrayerAlarmScheduler.schedule(context, true); }
            catch (RuntimeException error) { Log.e(TAG, "Could not chain next prayer event", error); }
            try { DhikrAlarmScheduler.schedule(context); }
            catch (RuntimeException error) { Log.e(TAG, "Could not refresh timed dhikr", error); }
        }
    }

    static boolean shouldStartAdhan(boolean wasExact, boolean exactPermissionAllowed,
                                     int sdkInt) {
        return sdkInt < 31 || (wasExact && exactPermissionAllowed);
    }

    static long expectedAlarmAt(Intent intent) {
        if (intent == null) return 0L;
        try {
            Uri data = intent.getData();
            if (data != null && !data.getPathSegments().isEmpty()) {
                return Long.parseLong(data.getPathSegments().get(0));
            }
        } catch (RuntimeException ignored) { }
        return 0L;
    }

    static boolean isAdhanTooLate(long expectedAt, long receivedAt) {
        return expectedAt > 0L && receivedAt - expectedAt > ADHAN_LATE_GRACE_MS;
    }

    static int remainingReminderMinutes(long prayerAt, long now) {
        if (prayerAt <= now) return 0;
        return (int) Math.max(1L, (prayerAt - now + MINUTE - 1L) / MINUTE);
    }

    private void recordDelivery(Context context, Intent intent, String type,
                                String key, long prayerAt) {
        long receivedAt = System.currentTimeMillis();
        long expectedAt = expectedAlarmAt(intent);
        long delay = expectedAt == 0L ? 0L : receivedAt - expectedAt;
        AlarmPrefs.get(context).edit()
                .putLong("lastAlarmExpectedAt", expectedAt)
                .putLong("lastAlarmReceivedAt", receivedAt)
                .putLong("lastAlarmDelayMs", delay)
                .putLong("lastAlarmPrayerAt", prayerAt)
                .putBoolean("lastAlarmWasExact",
                        intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_EXACT, true))
                .putString("lastAlarmType", type == null ? "" : type)
                .putString("lastAlarmKey", key == null ? "" : key).apply();
        Log.i(TAG, "Alarm delivered: type=" + type + ", key=" + key
                + ", exact=" + intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_EXACT, true)
                + ", delayMs=" + delay);
    }

    private void recordPlaybackFailure(Context context, String state,
                                       String detail, String prayerKey) {
        AlarmPrefs.get(context).edit()
                .putLong("lastAdhanPlaybackEventAt", System.currentTimeMillis())
                .putString("lastAdhanPlaybackState", state)
                .putString("lastAdhanPlaybackDetail", detail == null ? "" : detail)
                .putString("lastAdhanPlaybackPrayer", prayerKey == null ? "" : prayerKey)
                .apply();
    }

    private void showAdhanStartFailure(Context context, String name, String prayerKey,
                                        boolean backgroundStartBlocked) {
        String message = backgroundStartBlocked
                ? "تعذّر تشغيل الأذان في الخلفية بسبب قيود النظام. فعّل إذن المنبهات الدقيقة من إعدادات التطبيق لضمان وصول الأذان في موعده."
                : "تعذّر بدء خدمة تشغيل الأذان. افتح التطبيق للتحقق من أذونات التنبيهات وإعدادات البطارية.";
        showAdhanFailure(context,
                backgroundStartBlocked ? "foreground-service-start-not-allowed" : "service-start-failed",
                name, prayerKey, message);
    }

    /** Shared failure surface for alarm dispatch and failures occurring inside the playback service. */
    static void showAdhanFailure(Context context, String failureState, String prayerName,
                                 String prayerKey, String message) {
        if (context == null || failureState == null) return;
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot show adhan fallback notification: POST_NOTIFICATIONS is not granted");
            return;
        }
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(ADHAN_FAILURE_CHANNEL,
                    "تنبيه تعذّر الأذان", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("تنبيهات عالية الأولوية عند تعذّر تشغيل الأذان");
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
            manager.createNotificationChannel(channel);
            if (manager.getNotificationChannel(ADHAN_FAILURE_CHANNEL) != null
                    && manager.getNotificationChannel(ADHAN_FAILURE_CHANNEL).getImportance()
                    == NotificationManager.IMPORTANCE_NONE) return;
        }
        String safeKey = failureState + "|" + (prayerKey == null ? "" : prayerKey);
        SharedPreferences prefs = AlarmPrefs.get(context);
        long now = System.currentTimeMillis();
        String debounceKey = "lastAdhanFailureNoticeAt_" + Integer.toHexString(safeKey.hashCode());
        if (now - prefs.getLong(debounceKey, 0L) < 5000L) return;
        prefs.edit().putLong(debounceKey, now).apply();

        Intent openApp = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent content = PendingIntent.getActivity(context, 91, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, ADHAN_FAILURE_CHANNEL)
                : new Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH);
        String title = "تعذّر تشغيل أذان " + (prayerName == null || prayerName.isEmpty()
                ? "الصلاة" : prayerName);
        Notification notification = builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message == null || message.isEmpty()
                        ? "افتح نور الإيمان للتحقق من حالة تشغيل الأذان." : message)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(content)
                .build();
        manager.notify(safeKey.hashCode(), notification);
    }

    private void showReminder(Context context, String key, String name, String data,
                              int minutesRemaining) {
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(REMINDER_CHANNEL,
                    "تذكير الصلاة", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("تذكير قبل دخول وقت الصلاة بعشر دقائق");
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
            manager.createNotificationChannel(channel);
        }

        Intent openApp = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent content = PendingIntent.getActivity(context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, REMINDER_CHANNEL)
                : new Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH);
        Notification notification = builder
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("تذكير الصلاة")
                .setContentText(reminderText(minutesRemaining, name))
                .setCategory(Notification.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(content)
                .build();
        manager.notify((data + String.valueOf(key)).hashCode(), notification);
    }

    private static String reminderText(int minutes, String name) {
        String duration;
        if (minutes == 1) duration = "دقيقة واحدة";
        else if (minutes == 2) duration = "دقيقتان";
        else if (minutes >= 3 && minutes <= 10) duration = minutes + " دقائق";
        else duration = minutes + " دقيقة";
        return "بقيت " + duration + " على صلاة " + (name == null ? "" : name);
    }
}
