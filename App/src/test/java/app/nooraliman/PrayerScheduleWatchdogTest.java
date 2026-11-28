package app.nooraliman;

import org.junit.Test;

import static org.junit.Assert.*;

public class PrayerScheduleWatchdogTest {
    @Test public void repairsMissingExactAdhanAlarmsWhenExactAccessIsGranted() {
        assertTrue(PrayerScheduleWatchdog.shouldRepairSchedule(
                true, false, true, 20, 18, 0, 0,
                "scheduled_exact", 0L, 100L));
    }

    @Test public void doesNotLoopTryingToRepairAdhanWithoutExactAlarmAccess() {
        assertFalse(PrayerScheduleWatchdog.shouldRepairSchedule(
                true, false, false, 20, 0, 0, 0,
                "exact_permission_required", 0L, 100L));
    }

    @Test public void repairsMissingReminderAlarmsEvenWithoutExactAlarmAccess() {
        assertTrue(PrayerScheduleWatchdog.shouldRepairSchedule(
                true, true, false, 20, 0, 20, 17,
                "exact_permission_required", 0L, 100L));
    }

    @Test public void repairsFailedOrExpiredSchedules() {
        assertTrue(PrayerScheduleWatchdog.shouldRepairSchedule(
                true, false, true, 20, 20, 0, 0,
                "schedule_failed", 0L, 100L));
        assertTrue(PrayerScheduleWatchdog.shouldRepairSchedule(
                false, true, true, 0, 0, 20, 20,
                "scheduled_exact", 99L, 100L));
    }

    @Test public void doesNotRepairWhenBothAlertTypesAreDisabled() {
        assertFalse(PrayerScheduleWatchdog.shouldRepairSchedule(
                false, false, true, 0, 0, 0, 0,
                "disabled", 0L, 100L));
    }
}
