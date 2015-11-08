package ninja.eivind.hotsreplayuploader.providers;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;

public abstract class Provider {

    private final String name;
    private SimpleHttpClient httpClient = new SimpleHttpClient();
    public Provider(String name) {
        this.name = name;
    }

    protected final SimpleHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public abstract Status upload(ReplayFile replayFile);

    public String getName() {
        return name;
    }
}
