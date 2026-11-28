package app.nooraliman;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

/**
 * Best-effort recovery watchdog. AlarmManager remains the only time-critical
 * trigger for prayer events; this worker only repairs an unhealthy schedule.
 */
public final class PrayerScheduleWatchdog extends Worker {
    private static final String TAG = "NoorPrayerWatchdog";
    private static final String UNIQUE_WORK = "noor-prayer-schedule-watchdog";
    private static final long INTERVAL_HOURS = 6L;

    public PrayerScheduleWatchdog(Context context, WorkerParameters parameters) {
        super(context, parameters);
    }

    public static void ensureScheduled(Context context) {
        if (context == null) return;
        SharedPreferences prefs = AlarmPrefs.get(context);
        boolean enabled = prefs.getBoolean("adhan", false)
                || prefs.getBoolean("reminder", false);
        WorkManager manager = WorkManager.getInstance(context.getApplicationContext());
        if (!enabled) {
            manager.cancelUniqueWork(UNIQUE_WORK);
            return;
        }
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                PrayerScheduleWatchdog.class, INTERVAL_HOURS, TimeUnit.HOURS)
                .setInitialDelay(30L, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,
                        15L, TimeUnit.MINUTES)
                .build();
        manager.enqueueUniquePeriodicWork(
                UNIQUE_WORK, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    static boolean shouldRepairSchedule(boolean adhanEnabled, boolean reminderEnabled,
                                        boolean exactAllowed, int adhanRequested,
                                        int adhanScheduled, int reminderRequested,
                                        int reminderScheduled, String status,
                                        long nextRefreshAt, long now) {
        if (!adhanEnabled && !reminderEnabled) return false;
        if (adhanEnabled && exactAllowed && adhanRequested > adhanScheduled) return true;
        if (reminderEnabled && reminderRequested > reminderScheduled) return true;
        if ("schedule_failed".equals(status)
                || "partial_schedule_failure".equals(status)
                || "alarm_manager_unavailable".equals(status)
                || "no_future_alarms".equals(status)) return true;
        return nextRefreshAt > 0L && nextRefreshAt <= now;
    }

    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = AlarmPrefs.get(context);
        long now = System.currentTimeMillis();
        boolean adhanEnabled = prefs.getBoolean("adhan", false);
        boolean reminderEnabled = prefs.getBoolean("reminder", false);
        if (!adhanEnabled && !reminderEnabled) {
            prefs.edit().putLong("watchdogLastRunAt", now)
                    .putString("watchdogLastResult", "disabled").apply();
            return Result.success();
        }

        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        boolean exactAllowed = Build.VERSION.SDK_INT < 31
                || (alarms != null && alarms.canScheduleExactAlarms());
        boolean repair = shouldRepairSchedule(
                adhanEnabled, reminderEnabled, exactAllowed,
                prefs.getInt("prayerAdhanRequestedCount", 0),
                prefs.getInt("prayerAdhanScheduledCount", 0),
                prefs.getInt("prayerReminderRequestedCount", 0),
                prefs.getInt("prayerReminderScheduledCount", 0),
                prefs.getString("prayerAlarmScheduleStatus", "unknown"),
                prefs.getLong("prayer_schedule_refresh_at", 0L), now);

        prefs.edit().putLong("watchdogLastRunAt", now)
                .putBoolean("batteryOptimizationExempt",
                        Build.VERSION.SDK_INT < 23 || isBatteryExempt(context))
                .putString("watchdogLastAction", repair ? "repair_requested" : "health_check")
                .apply();

        if (!repair) {
            prefs.edit().putString("watchdogLastResult", exactAllowed
                    ? "schedule_healthy_or_not_due" : "exact_alarm_access_missing").apply();
            return Result.success();
        }

        try {
            PrayerAlarmScheduler.schedule(context, true);
            prefs.edit().putString("watchdogLastResult", "schedule_rebuilt")
                    .putLong("watchdogLastRepairAt", System.currentTimeMillis()).apply();
            Log.i(TAG, "Prayer schedule recovery completed; exactAllowed=" + exactAllowed);
            return Result.success();
        } catch (RuntimeException error) {
            prefs.edit().putString("watchdogLastResult", "repair_failed")
                    .putString("watchdogLastError", error.getClass().getSimpleName()
                            + ": " + String.valueOf(error.getMessage())).apply();
            Log.e(TAG, "Prayer schedule watchdog could not rebuild alarms", error);
            return Result.retry();
        }
    }

    private static boolean isBatteryExempt(Context context) {
        PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return power != null && power.isIgnoringBatteryOptimizations(context.getPackageName());
    }
}
