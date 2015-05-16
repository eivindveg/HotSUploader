package com.metacodestudio.hotsuploader.providers;

import com.metacodestudio.hotsuploader.models.ReplayFile;
import com.metacodestudio.hotsuploader.models.Status;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Provider {

    private static final SimpleHttpClient httpClient = new SimpleHttpClient();
    private String name;

    public Provider(String name) {
        this.name = name;
    }

    public static List<Provider> getAll() {
        List<Provider> providers = new ArrayList<>();
        providers.add(new HotsLogsProvider());
        providers.add(new HeroGGProvider());
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
