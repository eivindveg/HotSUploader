package ninja.eivind.hotsreplayuploader.utils;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertNotNull;

public class NetUtilsTest {

    @Test
    public void testStringEncoding() {
        URI uri = SimpleHttpClient.encode("http://test.test/?query=que ry");
        assertNotNull("We got an uri", uri);
    }
}
