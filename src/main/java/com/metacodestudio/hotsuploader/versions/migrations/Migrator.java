package com.metacodestudio.hotsuploader.versions.migrations;

import com.metacodestudio.hotsuploader.utils.StormHandler;

public abstract class Migrator {

    private final StormHandler stormHandler;

    public Migrator(final StormHandler stormHandler) {
        this.stormHandler = stormHandler;
    }

    public abstract boolean migrate();
}
