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

package ninja.eivind.hotsreplayuploader.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Hero {

    @JsonProperty("PrimaryName")
    private String primaryName;
    @JsonProperty("ImageURL")
    private String imageURL;

    public Hero() {
    }

    public Hero(final String primaryName, final String imageURL) {
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

        final Hero hero = (Hero) o;

        return !(primaryName != null ? !primaryName.equals(hero.primaryName) : hero.primaryName != null)
                && !(imageURL != null ? !imageURL.equals(hero.imageURL) : hero.imageURL != null);

    }

    @Override
    public int hashCode() {
        int result = primaryName != null ? primaryName.hashCode() : 0;
        result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
        return result;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public void setPrimaryName(final String primaryName) {
        this.primaryName = primaryName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(final String imageURL) {
        this.imageURL = imageURL;
    }

}
