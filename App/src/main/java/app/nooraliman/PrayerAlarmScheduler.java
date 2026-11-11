package app.nooraliman;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

final class PrayerAlarmScheduler {
    static final String PREFS = "noor_alarm_settings";
    static final String ACTION_FIRE = "app.nooraliman.action.FIRE";
    static final String ACTION_REFRESH = "app.nooraliman.action.REFRESH";
    static final String EXTRA_TYPE = "type";
    static final String EXTRA_KEY = "key";
    static final String EXTRA_NAME = "name";
    static final String EXTRA_SOUND = "sound";
    static final String EXTRA_PREVIEW = "preview";
    static final String EXTRA_EXACT = "exact";
    private static final String IDS = "scheduled_alarm_ids";
    private static final String SCHEDULE_SIGNATURE = "prayer_schedule_signature";
    private static final String NEXT_REFRESH_AT = "prayer_schedule_refresh_at";
    private static final String TAG = "NoorPrayerAlarm";
    private static final long MINUTE = 60_000L;
    private static final double R = Math.PI / 180.0;
    private static final int DAYS_AHEAD = 15;
    private static final String[][] PRAYERS = {
            {"fajr", "الفجر"}, {"dhuhr", "الظهر"}, {"asr", "العصر"},
            {"maghrib", "المغرب"}, {"isha", "العشاء"}
    };

    private PrayerAlarmScheduler() {}

    static void schedule(Context context) {
        schedule(context, false);
    }

    static void schedule(Context context, boolean force) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean adhanEnabled = prefs.getBoolean("adhan", false);
        boolean reminderEnabled = prefs.getBoolean("reminder", false);
        if (!adhanEnabled && !reminderEnabled) {
            cancel(context);
            return;
        }

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            cancelScheduled(context, null, prefs);
            prefs.edit().putBoolean("exactAlarmPermissionGranted", false)
                    .putString("prayerAlarmScheduleStatus", "alarm_manager_unavailable").apply();
            Log.e(TAG, "AlarmManager is unavailable; prayer alerts were not scheduled");
            return;
        }
        boolean exactAlarmsAllowed = Build.VERSION.SDK_INT < 31
                || manager.canScheduleExactAlarms();
        double lat = prefs.getFloat("latitude", 15.5007f);
        double lng = prefs.getFloat("longitude", 32.5599f);
        String timezoneId = prefs.getString("timezone", "Africa/Khartoum");
        String method = prefs.getString("method", "umm");
        String asr = prefs.getString("asr", "std");
        String sound = prefs.getString("adhanSound", "classic");
        String adjustments = prefs.getString("prayerAdjustments", "{}");
        TimeZone timezone = TimeZone.getTimeZone(timezoneId);
        long now = System.currentTimeMillis();
        String signature = scheduleSignature(lat, lng, timezoneId, method, asr,
                adhanEnabled, reminderEnabled, sound, adjustments, exactAlarmsAllowed);
        long refreshAt = nextRefreshAt(timezone, now);

        // Avoid cancelling a pending alarm when renderTimes() repeats at a prayer boundary.
        if (!shouldReschedule(force,
                signature.equals(prefs.getString(SCHEDULE_SIGNATURE, "")),
                prefs.getLong(NEXT_REFRESH_AT, 0L), now)) return;

        cancelScheduled(context, manager, prefs);
        JSONArray ids = new JSONArray();
        Calendar localDay = Calendar.getInstance(timezone);
        localDay.setTimeInMillis(now);
        localDay.set(Calendar.HOUR_OF_DAY, 12);
        localDay.set(Calendar.MINUTE, 0);
        localDay.set(Calendar.SECOND, 0);
        localDay.set(Calendar.MILLISECOND, 0);

        int requestedAlarms = 0;
        int scheduledAlarms = 0;
        if (!exactAlarmsAllowed) {
            Log.w(TAG, "Exact-alarm access is missing; user-facing prayer alerts are not scheduled");
        } else {
            for (int day = 0; day < DAYS_AHEAD; day++) {
                Calendar date = (Calendar) localDay.clone();
                date.add(Calendar.DAY_OF_MONTH, day);
                long[] prayerTimes = calculate(date, timezone, lat, lng, method, asr);
                for (int i = 0; i < PRAYERS.length; i++) {
                    if (prayerTimes[i] == Long.MIN_VALUE) continue;
                    String key = PRAYERS[i][0];
                    String name = PRAYERS[i][1];
                    long prayerAt = prayerTimes[i] + adjustmentMinutes(prefs, key) * MINUTE;
                    if (adhanEnabled && isFutureAlarm(prayerAt, now)) {
                        requestedAlarms++;
                        if (scheduleOne(context, manager, ids, prayerAt, "adhan", key, name)) {
                            scheduledAlarms++;
                        }
                    }
                    long reminderAt = prayerAt - 10 * MINUTE;
                    if (reminderEnabled && isFutureAlarm(reminderAt, now)) {
                        requestedAlarms++;
                        if (scheduleOne(context, manager, ids, reminderAt, "reminder", key, name)) {
                            scheduledAlarms++;
                        }
                    }
                }
            }
        }
        String scheduleStatus = !exactAlarmsAllowed ? "exact_permission_required"
                : requestedAlarms == 0 ? "no_future_alarms"
                : scheduledAlarms == requestedAlarms ? "scheduled_exact"
                : scheduledAlarms == 0 ? "schedule_failed" : "partial_schedule_failure";
        prefs.edit().putString(IDS, ids.toString())
                .putString(SCHEDULE_SIGNATURE, signature)
                .putLong(NEXT_REFRESH_AT, refreshAt)
                .putBoolean("exactAlarmPermissionGranted", exactAlarmsAllowed)
                .putString("prayerAlarmScheduleStatus", scheduleStatus)
                .apply();
        scheduleRefresh(context, manager, refreshAt);
        Log.i(TAG, "Schedule refreshed; exact=" + exactAlarmsAllowed
                + ", status=" + scheduleStatus + ", alarms=" + ids.length());
    }

    static boolean shouldUseExactAlarms(boolean exactAlarmsAllowed) {
        return exactAlarmsAllowed;
    }

    static boolean isFutureAlarm(long alarmAt, long now) {
        return alarmAt > now;
    }

    static boolean shouldReschedule(boolean force, boolean sameSignature,
                                    long nextRefreshAt, long now) {
        return force || !sameSignature || nextRefreshAt <= now;
    }

    private static String scheduleSignature(double lat, double lng, String timezone,
                                            String method, String asr, boolean adhan,
                                            boolean reminder, String sound, String adjustments,
                                            boolean exactAllowed) {
        return String.format(Locale.US, "%.6f|%.6f|%s|%s|%s|%s|%s|%s|%s|%s",
                lat, lng, timezone, method, asr, adhan, reminder, sound,
                adjustments, exactAllowed);
    }

    private static long nextRefreshAt(TimeZone timezone, long now) {
        Calendar next = Calendar.getInstance(timezone);
        next.setTimeInMillis(now);
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        next.add(Calendar.DAY_OF_MONTH, 1);
        return next.getTimeInMillis();
    }

    static void cancel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        cancelScheduled(context, manager, prefs);
    }

    private static void cancelScheduled(Context context, AlarmManager manager,
                                        SharedPreferences prefs) {
        try {
            JSONArray ids = new JSONArray(prefs.getString(IDS, "[]"));
            for (int i = 0; i < ids.length(); i++) {
                Intent intent = new Intent(context, PrayerAlarmReceiver.class)
                        .setAction(ACTION_FIRE)
                        .setData(Uri.parse(ids.getString(i)));
                PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
                if (pending != null) {
                    if (manager != null) manager.cancel(pending);
                    pending.cancel();
                }
            }
        } catch (Exception ignored) {
        }
        Intent refresh = new Intent(context, PrayerAlarmReceiver.class)
                .setAction(ACTION_REFRESH)
                .setData(Uri.parse("noor://alarm/refresh"));
        PendingIntent pendingRefresh = PendingIntent.getBroadcast(context, 0, refresh,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pendingRefresh != null) {
            if (manager != null) manager.cancel(pendingRefresh);
            pendingRefresh.cancel();
        }
        prefs.edit().remove(IDS).remove(SCHEDULE_SIGNATURE).remove(NEXT_REFRESH_AT).apply();
    }

    private static boolean scheduleOne(Context context, AlarmManager manager, JSONArray ids,
                                        long at, String type, String key, String name) {
        String uri = String.format(Locale.US, "noor://alarm/%d/%s/%s", at, key, type);
        Intent intent = new Intent(context, PrayerAlarmReceiver.class)
                .setAction(ACTION_FIRE)
                .setData(Uri.parse(uri))
                .putExtra(EXTRA_TYPE, type)
                .putExtra(EXTRA_KEY, key)
                .putExtra(EXTRA_NAME, name)
                .putExtra(EXTRA_EXACT, true);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending);
            ids.put(uri);
            return true;
        } catch (SecurityException denied) {
            pending.cancel();
            Log.e(TAG, "Exact prayer alarm rejected; no inexact fallback will be used: "
                    + uri, denied);
            return false;
        }
    }

    private static void scheduleRefresh(Context context, AlarmManager manager, long refreshAt) {
        Intent intent = new Intent(context, PrayerAlarmReceiver.class)
                .setAction(ACTION_REFRESH)
                .setData(Uri.parse("noor://alarm/refresh"));
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, refreshAt, pending);
        } catch (SecurityException denied) {
            pending.cancel();
            Log.e(TAG, "Could not schedule daily prayer refresh", denied);
        }
    }

    private static int adjustmentMinutes(android.content.SharedPreferences prefs, String key) {
        try {
            JSONObject offsets = new JSONObject(prefs.getString("prayerAdjustments", "{}"));
            return Math.max(-180, Math.min(180, offsets.optInt(key, 0)));
        } catch (Exception ignored) {
            return 0;
        }
    }

    static long[] calculate(Calendar date, TimeZone timezone, double lat, double lng,
                                    String method, String asr) {
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);
        Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(year, month, day, 0, 0, 0);
        double offset = timezone.getOffset(date.getTimeInMillis()) / 3_600_000.0;
        double jd = utc.getTimeInMillis() / 86_400_000.0 + 2_440_587.5 - lng / 360.0;
        Sun sun = sunPosition(jd + 0.5);
        double noon = 12 + offset - lng / 15.0 - sun.eqt;
        double fajrAngle = method.equals("egypt") ? 19.5 : method.equals("umm") ? 18.5 : 18.0;
        double asrAngle = asrAngle(lat, sun.dec, asr.equals("hanafi"));
        double maghrib = noon + hourAngle(0.833, sun.dec, lat);
        double[] hours = new double[5];
        hours[0] = noon - hourAngle(fajrAngle, sun.dec, lat);
        hours[1] = noon;
        hours[2] = noon + hourAngle(asrAngle, sun.dec, lat);
        hours[3] = maghrib;
        if (method.equals("umm")) {
            hours[4] = maghrib + 90.0 / 60.0;
            if (isRamadan(date)) hours[4] += 0.5;
        } else {
            double ishaAngle = method.equals("egypt") ? 17.5 : method.equals("karachi") ? 18.0 : 17.0;
            hours[4] = noon + hourAngle(ishaAngle, sun.dec, lat);
        }
        long[] times = new long[hours.length];
        long utcMidnight = utc.getTimeInMillis();
        for (int i = 0; i < hours.length; i++) {
            if (Double.isNaN(hours[i]) || Double.isInfinite(hours[i])) {
                times[i] = Long.MIN_VALUE;
            } else {
                times[i] = Math.round((utcMidnight + (hours[i] - offset) * 3_600_000.0) / MINUTE) * MINUTE;
            }
        }
        return times;
    }

    static double asrAngle(double latitude, double declination, boolean hanafi) {
        return -Math.atan(1.0 / ((hanafi ? 2 : 1)
                + Math.tan(Math.abs(latitude - declination) * R))) / R;
    }

    private static boolean isRamadan(Calendar date) {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                android.icu.util.IslamicCalendar hijri = new android.icu.util.IslamicCalendar(
                        android.icu.util.TimeZone.getTimeZone(date.getTimeZone().getID()),
                        new android.icu.util.ULocale("en@calendar=islamic-umalqura"));
                hijri.setCalculationType(
                        android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
                hijri.setTimeInMillis(date.getTimeInMillis());
                return hijri.get(android.icu.util.Calendar.MONTH) == 8;
            } catch (RuntimeException unavailableIcuCalendar) {
                Log.w(TAG, "Umm al-Qura ICU unavailable; using legacy fallback",
                        unavailableIcuCalendar);
            }
        }
        return isRamadanArithmeticFallback(date);
    }

    private static boolean isRamadanArithmeticFallback(Calendar date) {
        Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        double jd = Math.floor(utc.getTimeInMillis() / 86_400_000.0 + 2_440_587.5) + 0.5;
        double epoch = 1_948_439.5;
        int year = (int) Math.floor((30 * (jd - epoch) + 10_646) / 10_631);
        double firstMonth = 354.0 * (year - 1)
                + Math.floor((3 + 11 * year) / 30.0) + epoch;
        int month = Math.max(1, Math.min(12,
                (int) Math.ceil((jd - 29 - firstMonth) / 29.5) + 1));
        return month == 9;
    }

    private static double hourAngle(double angle, double declination, double latitude) {
        double value = (-sin(angle) - sin(declination) * sin(latitude))
                / (cos(declination) * cos(latitude));
        // Match the WebView calculation: an unreachable solar altitude is
        // unavailable, not a clamped zero- or twelve-hour angle.
        if (value < -1.0 || value > 1.0) return Double.NaN;
        return Math.acos(value) / R / 15.0;
    }

    private static Sun sunPosition(double jd) {
        double d = jd - 2_451_545.0;
        double g = mod(357.529 + 0.98560028 * d, 360);
        double q = mod(280.459 + 0.98564736 * d, 360);
        double longitude = mod(q + 1.915 * sin(g) + 0.02 * sin(2 * g), 360);
        double obliquity = 23.439 - 3.6e-7 * d;
        double rightAscension = mod(Math.atan2(cos(obliquity) * sin(longitude),
                cos(longitude)) / R / 15.0, 24);
        double equation = q / 15.0 - rightAscension;
        if (equation > 12) equation -= 24;
        if (equation < -12) equation += 24;
        return new Sun(Math.asin(sin(obliquity) * sin(longitude)) / R, equation);
    }

    private static double sin(double degrees) { return Math.sin(degrees * R); }
    private static double cos(double degrees) { return Math.cos(degrees * R); }
    private static double mod(double value, double divisor) {
        return ((value % divisor) + divisor) % divisor;
    }

    private static final class Sun {
        final double dec;
        final double eqt;
        Sun(double dec, double eqt) { this.dec = dec; this.eqt = eqt; }
    }
}
