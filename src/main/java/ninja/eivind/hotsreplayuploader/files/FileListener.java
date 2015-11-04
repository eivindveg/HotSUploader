package ninja.eivind.hotsreplayuploader.files;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;

public interface FileListener {

    void handle(ReplayFile file);

}
