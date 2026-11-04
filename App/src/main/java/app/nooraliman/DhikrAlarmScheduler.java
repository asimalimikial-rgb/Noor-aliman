package app.nooraliman;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.Calendar;

final class DhikrAlarmScheduler {
    static final String ACTION_FIRE = "app.nooraliman.action.DHIKR";
    static final String EXTRA_TYPE = "dhikr_type";
    static final String EXTRA_VOLUME = "dhikr_volume";
    private static final String DATA = "noor://dhikr/next";
    private static final long MINUTE = 60_000L;

    private DhikrAlarmScheduler() {}

    static void schedule(Context context) {
        cancel(context);
        android.content.SharedPreferences prefs = prefs(context);
        if (!prefs.getBoolean("dhikrOn", false)) return;
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) return;

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 31 && !manager.canScheduleExactAlarms()) return;
        Intent intent = new Intent(context, PrayerAlarmReceiver.class)
                .setAction(ACTION_FIRE).setData(Uri.parse(DATA));
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    nextAt(System.currentTimeMillis(), prefs), pending);
        } catch (SecurityException ignored) {
        }
    }

    static void cancel(Context context) {
        Intent intent = new Intent(context, PrayerAlarmReceiver.class)
                .setAction(ACTION_FIRE).setData(Uri.parse(DATA));
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pending == null) return;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pending);
        pending.cancel();
    }

    static void fire(Context context) {
        android.content.SharedPreferences prefs = prefs(context);
        if (!prefs.getBoolean("dhikrOn", false)) return;
        if (inWindow(System.currentTimeMillis(), prefs)) {
            Intent service = new Intent(context, DhikrPlaybackService.class)
                    .setAction(DhikrPlaybackService.ACTION_PLAY)
                    .putExtra(EXTRA_TYPE, prefs.getString("dhikrType", "subhan"))
                    .putExtra(EXTRA_VOLUME, prefs.getFloat("dhikrVol", 0.6f));
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
            else context.startService(service);
        }
        schedule(context);
    }

    private static long nextAt(long now, android.content.SharedPreferences prefs) {
        int every = Math.max(5, Math.min(120, prefs.getInt("dhikrEvery", 15)));
        long candidate = now + every * MINUTE;
        int start = clockMinutes(prefs.getString("dhikrFrom", "06:00"));
        int end = clockMinutes(prefs.getString("dhikrTo", "22:00"));
        for (int i = 0; i < 3; i++) {
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(candidate);
            int minute = date.get(Calendar.HOUR_OF_DAY) * 60 + date.get(Calendar.MINUTE);
            if (inside(minute, start, end)) return candidate;
            if (start <= end ? minute < start : minute > end && minute < start) {
                date.set(Calendar.HOUR_OF_DAY, start / 60);
                date.set(Calendar.MINUTE, start % 60);
            } else {
                date.add(Calendar.DAY_OF_MONTH, 1);
                date.set(Calendar.HOUR_OF_DAY, start / 60);
                date.set(Calendar.MINUTE, start % 60);
            }
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            candidate = date.getTimeInMillis();
        }
        return candidate;
    }

    private static boolean inWindow(long at, android.content.SharedPreferences prefs) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(at);
        int minute = date.get(Calendar.HOUR_OF_DAY) * 60 + date.get(Calendar.MINUTE);
        return inside(minute, clockMinutes(prefs.getString("dhikrFrom", "06:00")),
                clockMinutes(prefs.getString("dhikrTo", "22:00")));
    }

    private static boolean inside(int minute, int start, int end) {
        return start <= end ? minute >= start && minute <= end : minute >= start || minute <= end;
    }

    private static int clockMinutes(String value) {
        try {
            String[] parts = value.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return Math.max(0, Math.min(23, hour)) * 60 + Math.max(0, Math.min(59, minute));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static android.content.SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
    }
}
