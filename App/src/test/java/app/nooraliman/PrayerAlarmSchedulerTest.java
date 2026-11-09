package app.nooraliman;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrayerAlarmSchedulerTest {
    @Test public void standardAsrAngleUsesDegreeToRadianConversion() {
        double actual = PrayerAlarmScheduler.asrAngle(15.5, 0.0, false);
        assertEquals(-38.0569, actual, 0.01);
    }

    @Test public void hanafiAsrAngleIsLessNegativeThanStandard() {
        double standard = PrayerAlarmScheduler.asrAngle(15.5, 0.0, false);
        double hanafi = PrayerAlarmScheduler.asrAngle(15.5, 0.0, true);
        assertTrue(hanafi > standard);
    }

    @Test public void alarmOneMillisecondInFutureIsScheduled() {
        assertTrue(PrayerAlarmScheduler.isFutureAlarm(1_001L, 1_000L));
    }

    @Test public void alarmAtOrBeforeNowIsNotScheduled() {
        assertFalse(PrayerAlarmScheduler.isFutureAlarm(1_000L, 1_000L));
        assertFalse(PrayerAlarmScheduler.isFutureAlarm(999L, 1_000L));
    }

    @Test public void exactAlarmPermissionSelectsAlarmPrecisionWithoutDisablingAlerts() {
        assertTrue(PrayerAlarmScheduler.shouldUseExactAlarms(true));
        assertFalse(PrayerAlarmScheduler.shouldUseExactAlarms(false));
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
