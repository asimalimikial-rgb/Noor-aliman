package app.nooraliman;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class DeviceEventReceiver extends BroadcastReceiver {
    private static final String TAG = "NoorPrayerAlarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();

        if ("android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED".equals(action)) {
            AlarmManager manager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            boolean exactAllowed = Build.VERSION.SDK_INT < 31
                    || (manager != null && manager.canScheduleExactAlarms());
            Log.i(TAG, "Exact-alarm grant broadcast received; exact=" + exactAllowed);
            scheduleAsync(context, "exact-alarm-permission");
            return;
        }

        if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            scheduleAsync(context, action);
        }
    }

    private void scheduleAsync(Context context, String reason) {
        PendingResult pendingResult = goAsync();
        Thread worker = new Thread(() -> {
            try {
                try {
                    PrayerAlarmScheduler.schedule(context, true);
                } catch (RuntimeException error) {
                    Log.e(TAG, "Prayer alarm reschedule failed after " + reason, error);
                }
                try {
                    DhikrAlarmScheduler.schedule(context);
                } catch (RuntimeException error) {
                    Log.e(TAG, "Dhikr alarm reschedule failed after " + reason, error);
                }
                if (!"android.intent.action.LOCKED_BOOT_COMPLETED".equals(reason)) {
                    try {
                        PrayerScheduleWatchdog.ensureScheduled(context);
                    } catch (RuntimeException error) {
                        Log.e(TAG, "Could not ensure prayer schedule watchdog after " + reason, error);
                    }
                }
            } finally {
                pendingResult.finish();
            }
        }, "NoorAlarmReschedule");
        try {
            worker.start();
        } catch (RuntimeException startFailure) {
            pendingResult.finish();
            Log.e(TAG, "Could not start alarm reschedule worker", startFailure);
        }
    }
}
