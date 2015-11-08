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

package ninja.eivind.hotsreplayuploader.versions.migrations;

import ninja.eivind.hotsreplayuploader.utils.StormHandler;

public abstract class Migrator {

    private final StormHandler stormHandler;

    public Migrator(final StormHandler stormHandler) {
        this.stormHandler = stormHandler;
    }

    public abstract boolean migrate();
}
