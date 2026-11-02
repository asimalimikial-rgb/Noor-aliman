package app.nooraliman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceEventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PrayerAlarmScheduler.schedule(context);
        DhikrAlarmScheduler.schedule(context);
    }
}
