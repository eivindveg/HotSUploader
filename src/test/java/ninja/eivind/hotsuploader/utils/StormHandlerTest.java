package ninja.eivind.hotsuploader.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StormHandlerTest {

    public static final String NO_MATCH = "Invalid string does not match";
    public static final String MATCH = "Valid string matches";
    private StormHandler stormHandler;

    @Before
    public void setup() {
        stormHandler = new StormHandler();
    }

    @Test
    public void testHotsAccountFilterMatching() throws Exception {
        final String hotsAccountFilter = stormHandler.getHotSAccountFilter();

        String value = "1-Hero-1-";
        assertFalse(NO_MATCH, value.matches(hotsAccountFilter));

        value = "asdf";
        assertFalse(NO_MATCH, value.matches(hotsAccountFilter));

        value = "1-Hero-123456";
        assertFalse(NO_MATCH, value.matches(hotsAccountFilter));

        value = "1-Hero-1-123456";
        assertTrue(MATCH, value.matches(hotsAccountFilter));

        value = "2-Hero-2-1234567890";
        assertTrue(MATCH, value.matches(hotsAccountFilter));
    }
}
