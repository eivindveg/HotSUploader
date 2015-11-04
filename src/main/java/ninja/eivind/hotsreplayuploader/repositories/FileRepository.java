package ninja.eivind.hotsreplayuploader.repositories;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;

public interface FileRepository {

    void deleteReplay(ReplayFile replayFile);

    void updateReplay(ReplayFile file);
}
