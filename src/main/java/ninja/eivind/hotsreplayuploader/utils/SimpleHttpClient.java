package ninja.eivind.hotsreplayuploader.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class SimpleHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpClient.class);
    public static final String SPACE = "%20";

    /**
     * Encodes a given String url into a valid URI if possible
     * Method should be updated as required to make sure any valid String url that can be pasted into an address bar
     * will fit into a URI object
     *
     * @param url the url to encode
     * @return a newly built URI if at all possible
     */
    public static URI encode(final String url) {
        return URI.create(url.replaceAll(" ", SPACE));
    }

    public String simpleRequest(final String target) throws IOException {
        return simpleRequest(encode(target));
    }

    public String simpleRequest(final URI uri) throws IOException {
        return simpleRequest(uri.toURL());
    }

    public String simpleRequest(final URL url) throws IOException {
        LOG.info("Opening connection to " + url);
        InputStream inputStream = url.openStream();
        return IOUtils.readInputStream(inputStream);
    }
}
