package app.nooraliman;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class DeviceEventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if ("android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED".equals(action)) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            boolean exactAllowed = Build.VERSION.SDK_INT < 31
                    || (manager != null && manager.canScheduleExactAlarms());
            // Rebuild on both grant and revoke: use exact alarms when allowed,
            // otherwise keep prayer and dhikr alerts alive through inexact fallback.
            PrayerAlarmScheduler.schedule(context, true);
            DhikrAlarmScheduler.schedule(context);
            Log.i("NoorPrayerAlarm", "Exact-alarm permission changed; exact=" + exactAllowed
                    + "; fallback schedules rebuilt");
            return;
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            // The schedulers read saved preferences and no-op if disabled.
            // If exact access is unavailable, they retain inexact fallback alarms.
            PrayerAlarmScheduler.schedule(context, true);
            DhikrAlarmScheduler.schedule(context);
        }
    }
}
