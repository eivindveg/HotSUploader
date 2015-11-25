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

package ninja.eivind.hotsreplayuploader.utils;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;

import java.io.File;
import java.util.Comparator;

/**
 * {@link Comparator} for comparing two {@link ReplayFile}s.<br>
 * Will order by newest files first.
 */
public class ReplayFileComparator implements Comparator<ReplayFile> {
    @Override
    public int compare(ReplayFile o1, ReplayFile o2) {
        if(o1 == o2) {
            return 0;
        }

        File file1 = o1.getFile();
        File file2 = o2.getFile();

        int modified = -Long.compare(file1.lastModified(), file2.lastModified());
        if(modified != 0) {
            return modified;
        }

        throw new IllegalStateException("No two different replays can be equal.");
    }
}
