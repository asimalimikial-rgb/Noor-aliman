package app.nooraliman;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrayerAlarmSchedulerTest {
    @Test public void standardAsrAngleUsesDegreeToRadianConversion() {
        double actual = PrayerAlarmScheduler.asrAngle(15.5, 0.0, false);
        assertEquals(-38.0569, actual, 0.01);
    }

    @Test public void hanafiAsrAngleIsLowerThanStandard() {
        double standard = PrayerAlarmScheduler.asrAngle(15.5, 0.0, false);
        double hanafi = PrayerAlarmScheduler.asrAngle(15.5, 0.0, true);
        assertTrue(hanafi > standard);
    }

    @Test public void exactAlarmAccessIsRequiredForPrayerAlerts() {
        assertTrue(PrayerAlarmScheduler.shouldScheduleAlerts(true));
        assertFalse(PrayerAlarmScheduler.shouldScheduleAlerts(false));
    }

    @Test public void unchangedScheduleDoesNotCancelPendingAlarms() {
        assertFalse(PrayerAlarmScheduler.shouldReschedule(
                false, true, 2_000_000L, 1_000_000L));
    }

    @Test public void changedScheduleMustBeRebuilt() {
        assertTrue(PrayerAlarmScheduler.shouldReschedule(
                false, false, 2_000_000L, 1_000_000L));
    }

    @Test public void expiredDailyRefreshMustRebuildSchedule() {
        assertTrue(PrayerAlarmScheduler.shouldReschedule(
                false, true, 900_000L, 1_000_000L));
    }

    @Test public void explicitRefreshAlwaysRebuildsSchedule() {
        assertTrue(PrayerAlarmScheduler.shouldReschedule(
                true, true, 2_000_000L, 1_000_000L));
    }
}
