package app.nooraliman;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrayerAlarmReceiverTest {
    @Test public void adhanAtFiveMinuteBoundaryIsNotDropped() {
        long expected = 1_000_000L;
        assertFalse(PrayerAlarmReceiver.isAdhanTooLate(expected, expected + 5L * 60_000L));
    }

    @Test public void adhanBeyondFiveMinuteBoundaryIsDropped() {
        long expected = 1_000_000L;
        assertTrue(PrayerAlarmReceiver.isAdhanTooLate(expected, expected + 5L * 60_000L + 1L));
    }

    @Test public void reminderCountdownReflectsActualRemainingTime() {
        assertEquals(10, PrayerAlarmReceiver.remainingReminderMinutes(1_600_000L, 1_000_000L));
        assertEquals(1, PrayerAlarmReceiver.remainingReminderMinutes(1_060_000L, 1_000_000L));
        assertEquals(0, PrayerAlarmReceiver.remainingReminderMinutes(1_000_000L, 1_000_000L));
        assertEquals(0, PrayerAlarmReceiver.remainingReminderMinutes(900_000L, 1_000_000L));
    }
}
