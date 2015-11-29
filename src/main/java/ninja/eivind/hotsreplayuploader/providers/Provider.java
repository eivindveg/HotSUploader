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

package ninja.eivind.hotsreplayuploader.providers;

import ninja.eivind.hotsreplayuploader.models.ReplayFile;
import ninja.eivind.hotsreplayuploader.models.Status;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.stormparser.models.Replay;

import java.io.Closeable;

/**
 * Defines an interface for uploading a {@link ReplayFile}
 * to a provider like hotslogs.com.
 */
public abstract class Provider implements Closeable {

    private final String name;
    private SimpleHttpClient httpClient = new SimpleHttpClient();

    public Provider(String name) {
        this.name = name;
    }

    protected final SimpleHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public abstract Status upload(ReplayFile replayFile);

    public abstract Status getPreStatus(Replay replay);

    public String getName() {
        return name;
    }

    @Override
    public abstract void close();
}
