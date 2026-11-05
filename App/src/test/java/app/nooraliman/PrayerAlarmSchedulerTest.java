package app.nooraliman;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrayerAlarmSchedulerTest {
    @Test public void standardAsrAngleUsesDegreeToRadianConversion() {
        double actual = PrayerAlarmScheduler.asrAngle(15.5, 0.0, false);
        assertEquals(-42.1, actual, 0.2);
    }

    @Test public void hanafiAsrAngleIsLowerThanStandard() {
        double standard = PrayerAlarmScheduler.asrAngle(15.5, 0.0, false);
        double hanafi = PrayerAlarmScheduler.asrAngle(15.5, 0.0, true);
        assertTrue(hanafi < standard);
    }
}
