package app.nooraliman;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

final class DhikrAlarmScheduler {
    static final String ACTION_FIRE = "app.nooraliman.action.DHIKR";
    static final String EXTRA_TYPE = "dhikr_type";
    static final String EXTRA_VOLUME = "dhikr_volume";
    private static final String DATA = "noor://dhikr/next";
    private static final String SIGNATURE = "dhikr_schedule_signature";
    private static final String NEXT_AT = "dhikr_schedule_next_at";
    private static final long MINUTE = 60_000L;
    private static final long DELIVERY_GRACE = 90L * 60_000L;

    private DhikrAlarmScheduler() {}

    static void schedule(Context context) {
        android.content.SharedPreferences prefs = prefs(context);
        if (!prefs.getBoolean("dhikrOn", false)) {
            cancel(context);
            return;
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            Log.e("NoorDhikrAlarm", "AlarmManager is unavailable");
            return;
        }
        long now = System.currentTimeMillis();
        String signature = scheduleSignature(prefs);
        long scheduledAt = prefs.getLong(NEXT_AT, 0L);
        PendingIntent existing = pendingIntent(context, PendingIntent.FLAG_NO_CREATE);
        if (shouldKeepScheduled(signature.equals(prefs.getString(SIGNATURE, "")),
                scheduledAt, now, existing != null)) return;
        if (existing != null) {
            manager.cancel(existing);
            existing.cancel();
        }
        Intent intent = new Intent(context, PrayerAlarmReceiver.class)
                .setAction(ACTION_FIRE).setData(Uri.parse(DATA));
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long at = nextAt(now, prefs);
        try {
            manager.set(AlarmManager.RTC_WAKEUP, at, pending);
            prefs.edit().putString(SIGNATURE, signature).putLong(NEXT_AT, at)
                    .putString("dhikrAlarmMode", "inexact_non_allow_while_idle").apply();
            Log.i("NoorDhikrAlarm", "Scheduled inexact timed dhikr at " + at);
        } catch (RuntimeException failure) {
            pending.cancel();
            prefs.edit().remove(SIGNATURE).remove(NEXT_AT)
                    .putString("dhikrAlarmMode", "schedule_failed").apply();
            Log.e("NoorDhikrAlarm", "Could not schedule inexact timed dhikr", failure);
        }
    }

    static boolean shouldKeepScheduled(boolean sameSignature, long scheduledAt,
                                       long now, boolean pendingExists) {
        return sameSignature && pendingExists && scheduledAt > 0L
                && scheduledAt >= now - DELIVERY_GRACE;
    }

    private static String scheduleSignature(android.content.SharedPreferences prefs) {
        return String.format(Locale.US, "%d|%s|%s|%s",
                prefs.getInt("dhikrEvery", 15),
                prefs.getString("dhikrFrom", "06:00"),
                prefs.getString("dhikrTo", "22:00"),
                java.util.TimeZone.getDefault().getID());
    }

    static void cancel(Context context) {
        PendingIntent pending = pendingIntent(context, PendingIntent.FLAG_NO_CREATE);
        if (pending != null) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (manager != null) manager.cancel(pending);
            pending.cancel();
        }
        prefs(context).edit().remove(SIGNATURE).remove(NEXT_AT).remove("dhikrAlarmMode").apply();
    }

    private static PendingIntent pendingIntent(Context context, int mode) {
        Intent intent = new Intent(context, PrayerAlarmReceiver.class)
                .setAction(ACTION_FIRE).setData(Uri.parse(DATA));
        return PendingIntent.getBroadcast(context, 0, intent,
                mode | PendingIntent.FLAG_IMMUTABLE);
    }

    static void fire(Context context) {
        android.content.SharedPreferences prefs = prefs(context);
        // The alarm has been consumed. Clear its identity before scheduling the next interval.
        prefs.edit().remove(SIGNATURE).remove(NEXT_AT).apply();
        if (!prefs.getBoolean("dhikrOn", false)) return;
        if (inWindow(System.currentTimeMillis(), prefs)) {
            Intent service = new Intent(context, DhikrPlaybackService.class)
                    .setAction(DhikrPlaybackService.ACTION_PLAY)
                    .putExtra(EXTRA_TYPE, prefs.getString("dhikrType", "subhan"))
                    .putExtra(EXTRA_VOLUME, prefs.getFloat("dhikrVol", 0.6f));
            try {
                if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
                else context.startService(service);
            } catch (RuntimeException startFailure) {
                Log.e("NoorDhikrAlarm", "Could not start timed dhikr service", startFailure);
            }
        }
        schedule(context);
    }

    static long nextAt(long now, android.content.SharedPreferences prefs) {
        int every = Math.max(5, Math.min(120, prefs.getInt("dhikrEvery", 15)));
        long intervalMs = every * MINUTE;
        int start = clockMinutes(prefs.getString("dhikrFrom", "06:00"));
        int end = clockMinutes(prefs.getString("dhikrTo", "22:00"));

        // Equal boundaries explicitly mean an all-day window, anchored at midnight.
        if (start == end) {
            Calendar midnight = Calendar.getInstance();
            midnight.setTimeInMillis(now);
            midnight.set(Calendar.HOUR_OF_DAY, 0);
            midnight.set(Calendar.MINUTE, 0);
            midnight.set(Calendar.SECOND, 0);
            midnight.set(Calendar.MILLISECOND, 0);
            long elapsed = Math.max(0L, now - midnight.getTimeInMillis());
            long steps = (elapsed / intervalMs) + 1L;
            return midnight.getTimeInMillis() + steps * intervalMs;
        }

        // Anchor every interval to the configured start time, not to delivery time.
        Calendar anchor = Calendar.getInstance();
        anchor.setTimeInMillis(now);
        anchor.set(Calendar.HOUR_OF_DAY, start / 60);
        anchor.set(Calendar.MINUTE, start % 60);
        anchor.set(Calendar.SECOND, 0);
        anchor.set(Calendar.MILLISECOND, 0);

        if (anchor.getTimeInMillis() > now && start > end) {
            anchor.add(Calendar.DAY_OF_MONTH, -1);
        }

        long anchorMs = anchor.getTimeInMillis();
        long candidate;
        if (now < anchorMs) {
            candidate = anchorMs;
        } else {
            long steps = ((now - anchorMs) / intervalMs) + 1L;
            candidate = anchorMs + steps * intervalMs;
        }

        for (int i = 0; i < 3; i++) {
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(candidate);
            int minute = date.get(Calendar.HOUR_OF_DAY) * 60 + date.get(Calendar.MINUTE);
            if (inside(minute, start, end)) return candidate;
            if (start < end && minute < start) {
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

    static boolean inside(int minute, int start, int end) {
        if (start == end) return true;
        return start < end ? minute >= start && minute <= end
                : minute >= start || minute <= end;
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
        return AlarmPrefs.get(context);
    }
}
