package app.nooraliman;

import org.junit.Test;
import static org.junit.Assert.*;

public class DhikrPlaybackServiceTest {
    @Test public void preparationTimesOutAtTwentySeconds() {
        assertFalse(DhikrPlaybackService.isPreparationTimedOut(1_000L, 20_999L));
        assertTrue(DhikrPlaybackService.isPreparationTimedOut(1_000L, 21_000L));
    }

    @Test public void invalidPreparationTimestampDoesNotTimeout() {
        assertFalse(DhikrPlaybackService.isPreparationTimedOut(0L, 50_000L));
        assertFalse(DhikrPlaybackService.isPreparationTimedOut(2_000L, 1_000L));
    }
}
