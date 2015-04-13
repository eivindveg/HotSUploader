package com.metacodestudio.hotsuploader.utils;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertNotNull;

public class NetUtilsTest {

    @Test
    public void testStringEncoding() {
        URI uri = NetUtils.encode("http://test.test/?query=que ry");
        assertNotNull("We got an uri", uri);
    }
}
