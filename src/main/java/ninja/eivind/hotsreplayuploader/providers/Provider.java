// Copyright 2015-2016 Eivind Vegsundvåg
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

package ninja.eivind.hotsreplayuploader.providers;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.stormparser.models.Replay;
import org.springframework.stereotype.Component;

/**
 * Defines an interface for uploading a {@link ReplayFile}
 * to a provider like hotslogs.com.
 */
@Component
public abstract class Provider {

    private final String name;

    public Provider(String name) {
        this.name = name;
    }

    public abstract Status upload(ReplayFile replayFile);

    public abstract Status getPreStatus(Replay replay);

    public String getName() {
        return name;
    }
}
