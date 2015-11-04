package ninja.eivind.hotsreplayuploader.repositories;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;

import java.util.List;

public interface FileRepository {

    void deleteReplay(ReplayFile replayFile);

    void updateReplay(ReplayFile file);

    List<ReplayFile> getAll();
}
