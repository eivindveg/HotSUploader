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

package ninja.eivind.hotsreplayuploader.models.stringconverters;

import javafx.util.StringConverter;
import ninja.eivind.hotsreplayuploader.models.Hero;

/**
 * A {@link StringConverter} for {@link Hero} entities.<br>
 * Resolves a {@link Hero} based on its name.
 */
public class HeroConverter extends StringConverter<Hero> {

    @Override
    public String toString(final Hero hero) {
        if (hero == null) {
            return "";
        }
        return hero.getPrimaryName();
    }

    @Override
    public Hero fromString(final String primaryName) {
        final String imageURL = primaryName.replaceAll("[\\W_]", "");
        if (imageURL.equals("")) {
            return null;
        }

        return new Hero(primaryName, imageURL);
    }
}
