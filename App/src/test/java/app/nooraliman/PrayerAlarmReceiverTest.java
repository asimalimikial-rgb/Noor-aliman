package app.nooraliman;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrayerAlarmReceiverTest {
    @Test public void adhanWithinFiveMinuteGraceIsNotDropped() {
        assertFalse(PrayerAlarmReceiver.isAdhanTooLate(1_000_000L, 1_299_999L));
    }

    @Test public void adhanBeyondFiveMinuteGraceIsDropped() {
        assertTrue(PrayerAlarmReceiver.isAdhanTooLate(1_000_000L, 1_300_001L));
    }

    @Test public void reminderCountdownReflectsActualRemainingTime() {
        assertEquals(10, PrayerAlarmReceiver.remainingReminderMinutes(1_600_000L, 1_000_000L));
        assertEquals(1, PrayerAlarmReceiver.remainingReminderMinutes(1_060_000L, 1_000_000L));
        assertEquals(0, PrayerAlarmReceiver.remainingReminderMinutes(1_000_000L, 1_000_000L));
        assertEquals(0, PrayerAlarmReceiver.remainingReminderMinutes(900_000L, 1_000_000L));
    }
}
