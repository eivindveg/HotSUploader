// Copyright 2015 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ninja.eivind.hotsreplayuploader.repositories;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;

import java.io.File;
import java.util.List;

/**
 * Interface, that abstracts the interaction with {@link ReplayFile}s
 * like removing them or creating and updating a related upload status.
 * Handles all CRUD operation for persistable ReplayFile data.
 */
public interface FileRepository {

    /**
     * Deletes a {@link ReplayFile}'s physical {@link File}
     * and it's saved upload status information.
     * @param replayFile to delete
     */
    void deleteReplay(ReplayFile replayFile);

    /**
     * Persists or updates the status of a {@link ReplayFile}.
     * @param file the affected replay
     */
    void updateReplay(ReplayFile file);

    /**
     * Retrieves all available {@link ReplayFile}s
     * @return {@link List} of {@link ReplayFile}s
     */
    List<ReplayFile> getAll();

    void deleteByFileName(ReplayFile file);
}
