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

package ninja.eivind.hotsreplayuploader.versions;

import ninja.eivind.hotsreplayuploader.utils.Constants;

public class VersionHandshakeToken implements Comparable<VersionHandshakeToken> {
    private String applicationName;
    private String version;

    public VersionHandshakeToken() {
        applicationName = Constants.APPLICATION_NAME;
        version = ReleaseManager.class.getPackage().getImplementationVersion();
        if (version == null)
            version = "Snapshot";
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((applicationName == null) ? 0 : applicationName.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VersionHandshakeToken other = (VersionHandshakeToken) obj;
        if (applicationName == null) {
            if (other.applicationName != null)
                return false;
        } else if (!applicationName.equals(other.applicationName))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    @Override
    public int compareTo(VersionHandshakeToken o) {
        return -version.compareTo(o.version); //TODO use release comparator?
    }

    public boolean isLessThan(VersionHandshakeToken tokenB) {
        return compareTo(tokenB) == -1;
    }
}
