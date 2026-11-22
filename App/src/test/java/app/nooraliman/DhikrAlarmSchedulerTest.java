package app.nooraliman;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class DhikrAlarmSchedulerTest {
    @Test public void keepsUnchangedPendingDhikrAlarm() {
        assertTrue(DhikrAlarmScheduler.shouldKeepScheduled(
                true, 1_100_000L, 1_000_000L, true));
    }

    @Test public void rebuildsWhenDhikrSettingsChange() {
        assertFalse(DhikrAlarmScheduler.shouldKeepScheduled(
                false, 1_100_000L, 1_000_000L, true));
    }

    @Test public void rebuildsWhenPendingIntentIsMissing() {
        assertFalse(DhikrAlarmScheduler.shouldKeepScheduled(
                true, 1_100_000L, 1_000_000L, false));
    }

    @Test public void keepsRecentlyDueAlarmToAvoidCancellingAtDeliveryBoundary() {
        assertTrue(DhikrAlarmScheduler.shouldKeepScheduled(
                true, 950_000L, 1_000_000L, true));
    }

    @Test public void keepsInexactDhikrAlarmWithinExtendedDozeDeliveryWindow() {
        assertTrue(DhikrAlarmScheduler.shouldKeepScheduled(
                true, 5_000_000L, 10_000_000L, true));
    }

    @Test public void rebuildsAnAlarmThatIsStaleBeyondNinetyMinuteGrace() {
        assertFalse(DhikrAlarmScheduler.shouldKeepScheduled(
                true, 1_000_000L, 7_000_001L, true));
    }

    @Test public void doesNotKeepMissingOrInvalidScheduleTimestamp() {
        assertFalse(DhikrAlarmScheduler.shouldKeepScheduled(
                true, 0L, 1_000_000L, true));
    }
    @Test public void equalStartAndEndMeansAllDayForEveryMinute() {
        for (int minute = 0; minute < 24 * 60; minute++) {
            assertTrue("minute " + minute, DhikrAlarmScheduler.inside(minute, 360, 360));
        }
    }

    @Test public void delayedDeliveryKeepsTheOriginalIntervalGrid() {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Context context = RuntimeEnvironment.getApplication();
            SharedPreferences prefs = context.getSharedPreferences(
                    "dhikr_scheduler_test", Context.MODE_PRIVATE);
            prefs.edit().clear().putInt("dhikrEvery", 15)
                    .putString("dhikrFrom", "06:00").putString("dhikrTo", "22:00").commit();

            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            now.clear();
            now.set(2026, Calendar.NOVEMBER, 22, 6, 19, 0);
            Calendar expected = (Calendar) now.clone();
            expected.set(Calendar.MINUTE, 30);
            assertEquals(expected.getTimeInMillis(),
                    DhikrAlarmScheduler.nextAt(now.getTimeInMillis(), prefs));
        } finally {
            TimeZone.setDefault(previous);
        }
    }

    @Test public void overnightWindowContinuesAcrossMidnight() {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Context context = RuntimeEnvironment.getApplication();
            SharedPreferences prefs = context.getSharedPreferences(
                    "dhikr_overnight_test", Context.MODE_PRIVATE);
            prefs.edit().clear().putInt("dhikrEvery", 15)
                    .putString("dhikrFrom", "22:00").putString("dhikrTo", "06:00").commit();

            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            now.clear();
            now.set(2026, Calendar.NOVEMBER, 23, 5, 50, 0);
            Calendar expected = (Calendar) now.clone();
            expected.set(Calendar.HOUR_OF_DAY, 6);
            expected.set(Calendar.MINUTE, 0);
            assertEquals(expected.getTimeInMillis(),
                    DhikrAlarmScheduler.nextAt(now.getTimeInMillis(), prefs));
        } finally {
            TimeZone.setDefault(previous);
        }
    }

}
