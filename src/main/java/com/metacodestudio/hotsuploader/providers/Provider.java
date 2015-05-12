package com.metacodestudio.hotsuploader.providers;

import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Provider {

    private String name;
    private static final SimpleHttpClient httpClient = new SimpleHttpClient();

    public Provider(String name) {
        this.name = name;
    }

    public abstract Status upload(ReplayFile replayFile);

    public static List<Provider> getAll() {
        // TODO ADD MORE PROVIDERS
        List<Provider> providers = new ArrayList<>();
        providers.add(new HotSLogs());
        return providers;
    }

    public String getName() {
        return name;
    }

    protected static final SimpleHttpClient getHttpClient() {
        return httpClient;
    }
}
