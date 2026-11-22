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

    @Test public void adhanLateGraceAllowsExactlyFiveMinutes() {
        long expected = 1_000_000L;
        assertFalse(PrayerAlarmReceiver.isAdhanTooLate(expected, expected + 5L * 60_000L));
        assertTrue(PrayerAlarmReceiver.isAdhanTooLate(expected, expected + 5L * 60_000L + 1L));
    }

    @Test public void exactAlarmPermissionSelectsAlarmPrecision() {
        assertTrue(PrayerAlarmScheduler.shouldUseExactAlarms(true));
        assertFalse(PrayerAlarmScheduler.shouldUseExactAlarms(false));
    }

    @Test public void prayerAlertsRemainSchedulableWithoutExactAlarmPermission() {
        assertTrue(PrayerAlarmScheduler.shouldSchedulePrayerAlerts(true));
        assertTrue(PrayerAlarmScheduler.shouldSchedulePrayerAlerts(false));
    }

    @Test public void nextEventSelectionKeepsTheEarliestFutureEvent() {
        long now = 1_000_000L;
        PrayerAlarmScheduler.AlarmEvent later = new PrayerAlarmScheduler.AlarmEvent(
                1_500_000L, 1_500_000L, "adhan", "asr", "العصر");
        PrayerAlarmScheduler.AlarmEvent earlier = new PrayerAlarmScheduler.AlarmEvent(
                1_200_000L, 1_800_000L, "reminder", "maghrib", "المغرب");
        assertSame(earlier, PrayerAlarmScheduler.earlierFutureEvent(later, earlier, now));
        assertSame(later, PrayerAlarmScheduler.earlierFutureEvent(later,
                new PrayerAlarmScheduler.AlarmEvent(900_000L, 900_000L,
                        "adhan", "fajr", "الفجر"), now));
    }

    @Test public void failedExactAlarmRetriesBeforeDailyRefresh() {
        assertEquals(1_900_000L,
                PrayerAlarmScheduler.retryRefreshAt(5_000_000L, 1_000_000L));
        assertEquals(1_500_000L,
                PrayerAlarmScheduler.retryRefreshAt(1_500_000L, 1_000_000L));
    }

    @Test public void consumedOrMissedNextEventForcesScheduleRebuild() {
        assertTrue(PrayerAlarmScheduler.isEventExpired(1_000L, 1_000L));
        assertTrue(PrayerAlarmScheduler.isEventExpired(900L, 1_000L));
        assertFalse(PrayerAlarmScheduler.isEventExpired(1_100L, 1_000L));
        assertFalse(PrayerAlarmScheduler.isEventExpired(0L, 1_000L));
    }

    @Test public void chainedReminderCarriesActualPrayerTime() {
        PrayerAlarmScheduler.AlarmEvent event = new PrayerAlarmScheduler.AlarmEvent(
                1_100_000L, 1_700_000L, "reminder", "dhuhr", "الظهر");
        assertEquals(1_100_000L, event.at);
        assertEquals(1_700_000L, event.prayerAt);
        assertTrue(event.uri().contains("/dhuhr/reminder"));
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

    private static long[] expectedUtc(String... values) {
        long[] result = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = java.time.Instant.parse(values[i]).toEpochMilli();
        }
        return result;
    }

    private static long[] calculateFor(String method, String asr, int year, int month, int day) {
        java.util.TimeZone timezone = java.util.TimeZone.getTimeZone("Africa/Khartoum");
        java.util.Calendar date = new java.util.GregorianCalendar(timezone);
        date.clear();
        date.set(year, month, day, 12, 0, 0);
        return PrayerAlarmScheduler.calculate(date, timezone, 18.45352, 31.81315, method, asr);
    }

    @Test public void javaPrayerTimesMatchWebViewForMeroeOnNovember2026() {
        assertArrayEquals(expectedUtc(
                "2026-11-09T02:41:00Z", "2026-11-09T09:37:00Z",
                "2026-11-09T12:52:00Z", "2026-11-09T15:17:00Z",
                "2026-11-09T16:28:00Z"),
                calculateFor("mwl", "std", 2026, java.util.Calendar.NOVEMBER, 9));
        assertArrayEquals(expectedUtc(
                "2026-11-09T02:39:00Z", "2026-11-09T09:37:00Z",
                "2026-11-09T12:52:00Z", "2026-11-09T15:17:00Z",
                "2026-11-09T16:47:00Z"),
                calculateFor("umm", "std", 2026, java.util.Calendar.NOVEMBER, 9));
        assertArrayEquals(expectedUtc(
                "2026-11-09T02:35:00Z", "2026-11-09T09:37:00Z",
                "2026-11-09T12:52:00Z", "2026-11-09T15:17:00Z",
                "2026-11-09T16:30:00Z"),
                calculateFor("egypt", "std", 2026, java.util.Calendar.NOVEMBER, 9));
        assertArrayEquals(expectedUtc(
                "2026-11-09T02:41:00Z", "2026-11-09T09:37:00Z",
                "2026-11-09T12:52:00Z", "2026-11-09T15:17:00Z",
                "2026-11-09T16:32:00Z"),
                calculateFor("karachi", "std", 2026, java.util.Calendar.NOVEMBER, 9));
    }

    @Test public void standardAndHanafiAsrMatchWebViewCalculation() {
        assertEquals(java.time.Instant.parse("2026-11-09T12:52:00Z").toEpochMilli(),
                calculateFor("mwl", "std", 2026, java.util.Calendar.NOVEMBER, 9)[2]);
        assertEquals(java.time.Instant.parse("2026-11-09T13:41:00Z").toEpochMilli(),
                calculateFor("mwl", "hanafi", 2026, java.util.Calendar.NOVEMBER, 9)[2]);
        assertEquals(java.time.Instant.parse("2027-07-15T13:17:00Z").toEpochMilli(),
                calculateFor("mwl", "std", 2027, java.util.Calendar.JULY, 15)[2]);
        assertEquals(java.time.Instant.parse("2027-07-15T14:33:00Z").toEpochMilli(),
                calculateFor("mwl", "hanafi", 2027, java.util.Calendar.JULY, 15)[2]);
    }

}
