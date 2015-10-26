package ninja.eivind.utils;

import ninja.eivind.hotsuploader.utils.SimpleHttpClient;

import static org.mockito.Mockito.mock;

/**
 * @author Eivind Vegsundvåg
 */
public class SimpleHttpClientTestUtils {

    private SimpleHttpClientTestUtils() {
    }

    public static SimpleHttpClient getBaseMock() {
        return mock(SimpleHttpClient.class);
    }
}
