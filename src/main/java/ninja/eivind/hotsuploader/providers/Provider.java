package ninja.eivind.hotsuploader.providers;

import ninja.eivind.hotsuploader.models.ReplayFile;
import ninja.eivind.hotsuploader.models.Status;
import ninja.eivind.hotsuploader.utils.SimpleHttpClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Provider {

    private static final SimpleHttpClient httpClient = new SimpleHttpClient();
    private final String name;

    public Provider(String name) {
        this.name = name;
    }

    public static List<Provider> getAll() {
        // TODO ADD MORE PROVIDERS
        List<Provider> providers = new ArrayList<>();
        providers.add(new HotsLogsProvider());
        return providers;
    }

    protected static final SimpleHttpClient getHttpClient() {
        return httpClient;
    }

    public abstract Status upload(ReplayFile replayFile);

    public String getName() {
        return name;
    }
}
