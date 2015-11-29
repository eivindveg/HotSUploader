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

import ninja.eivind.hotsreplayuploader.providers.Provider;
import ninja.eivind.hotsreplayuploader.providers.hotslogs.HotsLogsProvider;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Simple implementation containing only the {@link HotsLogsProvider}.
 */
public class SingletonListProviderRepository implements ProviderRepository, Closeable {

    private final List<Provider> all = Collections.singletonList(new HotsLogsProvider());

    @Override
    public Collection<Provider> getAll() {
        return all;
    }

    @Override
    public void close() {
        all.forEach(Provider::close);
    }
}
