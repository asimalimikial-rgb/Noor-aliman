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

public class PrayerAlarmReceiver extends BroadcastReceiver {
    private static final String REMINDER_CHANNEL = "prayer_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PrayerAlarmScheduler.ACTION_REFRESH.equals(intent.getAction())) {
            PrayerAlarmScheduler.schedule(context);
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
        SharedPreferences prefs = context.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
        if ("adhan".equals(type) && prefs.getBoolean("adhan", false)) {
            Intent service = new Intent(context, AdhanPlaybackService.class)
                    .setAction(AdhanPlaybackService.ACTION_PLAY)
                    .putExtra(PrayerAlarmScheduler.EXTRA_NAME, name)
                    .putExtra(PrayerAlarmScheduler.EXTRA_SOUND,
                            prefs.getString("adhanSound", "classic"));
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
            else context.startService(service);
        } else if ("reminder".equals(type) && prefs.getBoolean("reminder", false)) {
            showReminder(context, key, name, intent.getDataString());
        }
    }

    private void showReminder(Context context, String key, String name, String data) {
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(REMINDER_CHANNEL,
                    "تذكير الصلاة", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("تذكير قبل دخول وقت الصلاة بعشر دقائق");
            channel.enableVibration(true);
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
                .setCategory(Notification.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(content)
                .build();
        manager.notify((data + String.valueOf(key)).hashCode(), notification);
    }
}
