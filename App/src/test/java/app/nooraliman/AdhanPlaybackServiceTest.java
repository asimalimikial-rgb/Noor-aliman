package app.nooraliman;

import org.junit.Test;
import static org.junit.Assert.*;

public class AdhanPlaybackServiceTest {
    @Test public void screenDownIsDetected() {
        assertTrue(AdhanPlaybackService.isFaceDown(0f, 0f, -9.8f));
    }
    @Test public void screenUpIsNotDetected() {
        assertFalse(AdhanPlaybackService.isFaceDown(0f, 0f, 9.8f));
    }
    @Test public void uprightIsNotDetected() {
        assertFalse(AdhanPlaybackService.isFaceDown(9.8f, 0f, 0f));
    }
    @Test public void mediaPreparationTimesOutAtTwentySeconds() {
        assertFalse(AdhanPlaybackService.isPreparationTimedOut(1_000L, 20_999L));
        assertTrue(AdhanPlaybackService.isPreparationTimedOut(1_000L, 21_000L));
    }

    @Test public void invalidPreparationStartDoesNotTimeout() {
        assertFalse(AdhanPlaybackService.isPreparationTimedOut(0L, 50_000L));
        assertFalse(AdhanPlaybackService.isPreparationTimedOut(2_000L, 1_000L));
    }

    @Test public void unstableMagnitudeIsNotDetected() {
        assertFalse(AdhanPlaybackService.isFaceDown(0f, 0f, -20f));
    }
}
