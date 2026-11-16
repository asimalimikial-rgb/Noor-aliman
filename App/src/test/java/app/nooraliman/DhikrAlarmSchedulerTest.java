package app.nooraliman;

import org.junit.Test;

import static org.junit.Assert.*;

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

    @Test public void rebuildsAnAlarmThatIsStaleBeyondDeliveryGrace() {
        assertFalse(DhikrAlarmScheduler.shouldKeepScheduled(
                true, 800_000L, 1_000_000L, true));
    }

    @Test public void doesNotKeepMissingOrInvalidScheduleTimestamp() {
        assertFalse(DhikrAlarmScheduler.shouldKeepScheduled(
                true, 0L, 1_000_000L, true));
    }
}
