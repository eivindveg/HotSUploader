package com.metacodestudio.hotsuploader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class NetUtils {

    private NetUtils() {
    }

    public static String simpleRequest(final String target) throws IOException {
        return simpleRequest(URI.create(target));
    }

    public static String simpleRequest(final URI uri) throws IOException {
        return simpleRequest(uri.toURL());
    }

    public static String simpleRequest(final URL url) throws IOException {
        InputStream inputStream = url.openStream();
        return IOUtils.readInputStream(inputStream);
    }
}
