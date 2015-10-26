package ninja.eivind.hotsreplayuploader.versions.migrations;

import ninja.eivind.hotsreplayuploader.utils.StormHandler;

public abstract class Migrator {

    private final StormHandler stormHandler;

    public Migrator(final StormHandler stormHandler) {
        this.stormHandler = stormHandler;
    }

    public abstract boolean migrate();
}
