package com.metacodestudio.hotsuploader.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;

public class NetUtils {

    private static final HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();

    private NetUtils() {
    }

    public static String simpleRequest(String target) throws IOException {
        return requestFactory.buildGetRequest(new GenericUrl(target)).execute().parseAsString();
    }
}
