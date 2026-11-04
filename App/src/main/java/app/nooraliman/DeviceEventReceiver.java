package app.nooraliman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceEventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            // The schedulers read saved preferences and safely no-op if disabled
            // or if exact-alarm access has not been granted.
            PrayerAlarmScheduler.schedule(context);
            DhikrAlarmScheduler.schedule(context);
        }
    }
}
