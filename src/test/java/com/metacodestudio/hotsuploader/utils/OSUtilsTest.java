package com.metacodestudio.hotsuploader.utils;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class OSUtilsTest {

    public static final String NO_MATCH = "Invalid string does not match";
    public static final String MATCH = "Valid string matches";

    @Test
    public void testHotsAccountFilterMatching() throws Exception {
        final Field field = OSUtils.class.getDeclaredField("HOTS_ACCOUNT_FILTER");
        field.setAccessible(true);
        final String hotsAccountFilter = (String) field.get(OSUtils.class);

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
