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

package ninja.eivind.hotsreplayuploader.providers.hotslogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ninja.eivind.hotsreplayuploader.models.Hero;

/**
 * API object retrieved from HotSLogs.com calls, which represents a playable hero primarily by its name.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotSLogsHero implements Hero {

    @JsonProperty("PrimaryName")
    private String primaryName;
    @JsonProperty("ImageURL")
    private String imageURL;

    public HotSLogsHero() {
    }

    public HotSLogsHero(final String primaryName, final String imageURL) {
        this.primaryName = primaryName;
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return primaryName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final HotSLogsHero hero = (HotSLogsHero) o;

        return !(primaryName != null ? !primaryName.equals(hero.primaryName) : hero.primaryName != null)
                && !(imageURL != null ? !imageURL.equals(hero.imageURL) : hero.imageURL != null);

    }

    @Override
    public int hashCode() {
        int result = primaryName != null ? primaryName.hashCode() : 0;
        result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
        return result;
    }

    @Override
    public String getPrimaryName() {
        return primaryName;
    }

    @Override
    public void setPrimaryName(final String primaryName) {
        this.primaryName = primaryName;
    }

    @Override
    public String getImageURL() {
        return imageURL;
    }

    @Override
    public void setImageURL(final String imageURL) {
        this.imageURL = imageURL;
    }

}
