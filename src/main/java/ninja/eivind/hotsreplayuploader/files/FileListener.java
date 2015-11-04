package ninja.eivind.hotsreplayuploader.files;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;

import java.io.File;

public interface FileListener {

    void handle(ReplayFile file);

}
