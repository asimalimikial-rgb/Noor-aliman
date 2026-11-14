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
    private static final String TAG = "NoorPrayerAlarm";

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
        String type = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TYPE);
        String key = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_KEY);
        String name = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_NAME);
        recordDelivery(context, intent, type, key);
        SharedPreferences prefs = context.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
        boolean wasExact = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_EXACT, true);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 31
                && (!wasExact || alarms == null || !alarms.canScheduleExactAlarms())) {
            Log.w(TAG, "Dropped prayer alert because exact-alarm access is unavailable or event is inexact: " + type + "/" + key);
            if ("adhan".equals(type)) {
                recordPlaybackFailure(context, "exact-alarm-access-missing",
                        "Exact-alarm access is required; approximate adhan events are ignored", key);
                showAdhanFailure(context, "exact-alarm-access-missing", name, key,
                        "فعّل إذن التنبيهات الدقيقة من إعدادات أندرويد لتشغيل الأذان في موعده.");
            }
            return;
        }
        if ("adhan".equals(type) && prefs.getBoolean("adhan", false)) {
            Intent service = new Intent(context, AdhanPlaybackService.class)
                    .setAction(AdhanPlaybackService.ACTION_PLAY)
                    .putExtra(PrayerAlarmScheduler.EXTRA_NAME, name)
                    .putExtra(PrayerAlarmScheduler.EXTRA_KEY, key)
                    .putExtra(PrayerAlarmScheduler.EXTRA_SOUND,
                            prefs.getString("adhanSound", "classic"));
            try {
                if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
                else context.startService(service);
            } catch (RuntimeException startFailure) {
                // Never let a restricted background-service launch fail silently.
                Log.e(TAG, "Could not start adhan playback service for " + key, startFailure);
                recordPlaybackFailure(context, "foreground-service-start-failed",
                        startFailure.toString(), key);
                showAdhanStartFailure(context, name, key);
            }
        } else if ("reminder".equals(type) && prefs.getBoolean("reminder", false)) {
            showReminder(context, key, name, intent.getDataString());
        }
    }

    private void recordDelivery(Context context, Intent intent, String type, String key) {
        long receivedAt = System.currentTimeMillis();
        long expectedAt = 0L;
        try {
            Uri data = intent.getData();
            if (data != null && !data.getPathSegments().isEmpty()) {
                expectedAt = Long.parseLong(data.getPathSegments().get(0));
            }
        } catch (RuntimeException ignored) { }
        long delay = expectedAt == 0L ? 0L : receivedAt - expectedAt;
        context.getSharedPreferences(PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE).edit()
                .putLong("lastAlarmExpectedAt", expectedAt)
                .putLong("lastAlarmReceivedAt", receivedAt)
                .putLong("lastAlarmDelayMs", delay)
                .putBoolean("lastAlarmWasExact",
                        intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_EXACT, true))
                .putString("lastAlarmType", type == null ? "" : type)
                .putString("lastAlarmKey", key == null ? "" : key)
                .apply();
        Log.i(TAG, "Alarm delivered: type=" + type + ", key=" + key
                + ", exact=" + intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_EXACT, true)
                + ", delayMs=" + delay);
    }

    private void recordPlaybackFailure(Context context, String state,
                                       String detail, String prayerKey) {
        context.getSharedPreferences(PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE).edit()
                .putLong("lastAdhanPlaybackEventAt", System.currentTimeMillis())
                .putString("lastAdhanPlaybackState", state)
                .putString("lastAdhanPlaybackDetail", detail == null ? "" : detail)
                .putString("lastAdhanPlaybackPrayer", prayerKey == null ? "" : prayerKey)
                .apply();
    }

    private void showAdhanStartFailure(Context context, String name, String prayerKey) {
        showAdhanFailure(context, "service-start-failed", name, prayerKey,
                "تعذّر بدء خدمة تشغيل الأذان. تحقق من إعدادات البطارية والتنبيهات.");
    }

    /** Shared failure surface for alarm dispatch and failures occurring inside the playback service. */
    static void showAdhanFailure(Context context, String failureState, String prayerName,
                                 String prayerKey, String message) {
        if (context == null || failureState == null) return;
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(REMINDER_CHANNEL,
                    "تذكير الصلاة", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("تنبيهات حالة الأذان");
            manager.createNotificationChannel(channel);
            if (manager.getNotificationChannel(REMINDER_CHANNEL) != null
                    && manager.getNotificationChannel(REMINDER_CHANNEL).getImportance()
                    == NotificationManager.IMPORTANCE_NONE) return;
        }
        String safeKey = failureState + "|" + (prayerKey == null ? "" : prayerKey);
        SharedPreferences prefs = context.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        String debounceKey = "lastAdhanFailureNoticeAt_" + Integer.toHexString(safeKey.hashCode());
        if (now - prefs.getLong(debounceKey, 0L) < 5000L) return;
        prefs.edit().putLong(debounceKey, now).apply();

        Intent openApp = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent content = PendingIntent.getActivity(context, 91, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, REMINDER_CHANNEL)
                : new Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH);
        String title = "تعذّر تشغيل أذان " + (prayerName == null || prayerName.isEmpty()
                ? "الصلاة" : prayerName);
        Notification notification = builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message == null || message.isEmpty()
                        ? "افتح نور الإيمان للتحقق من حالة تشغيل الأذان." : message)
                .setCategory(Notification.CATEGORY_ERROR)
                .setAutoCancel(true)
                .setContentIntent(content)
                .build();
        manager.notify(safeKey.hashCode(), notification);
    }

    private void showReminder(Context context, String key, String name, String data) {
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
                .setContentText("بقيت عشر دقائق على صلاة " + (name == null ? "" : name))
                .setCategory(Notification.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(content)
                .build();
        manager.notify((data + String.valueOf(key)).hashCode(), notification);
    }
}
