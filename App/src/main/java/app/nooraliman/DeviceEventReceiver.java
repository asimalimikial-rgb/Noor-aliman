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
            // Android sends this broadcast when exact-alarm access is granted.
            // If access is unavailable, prayer alarms are not scheduled; dhikr has
            // its own inexact fallback implementation.
            PrayerAlarmScheduler.schedule(context, true);
            DhikrAlarmScheduler.schedule(context);
            Log.i("NoorPrayerAlarm", "Exact-alarm grant broadcast received; exact=" + exactAllowed
                    + "; schedules rebuilt according to each scheduler's policy");
            return;
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            // The schedulers read saved preferences and no-op if disabled.
            // Prayer alerts require exact-alarm access; dhikr may use its own fallback.
            PrayerAlarmScheduler.schedule(context, true);
            DhikrAlarmScheduler.schedule(context);
        }
    }
}
