package com.metacodestudio.hotsuploader.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class DesktopWrapper {

    private Desktop desktop;

    public DesktopWrapper() {
        if (Desktop.isDesktopSupported() && !StormHandler.isLinux()) {
            desktop = Desktop.getDesktop();
        }
    }

    public void browse(URI uri) throws IOException {
        if (usingNative()) {
            desktop.browse(uri);
        } else {
            Runtime.getRuntime().exec("xdg-open " + uri.toURL());
        }
    }

    private boolean usingNative() {
        return desktop != null;
    }
}
