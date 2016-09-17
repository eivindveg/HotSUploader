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
import ninja.eivind.hotsreplayuploader.models.Account;

/**
 * A {@link StringConverter} for {@link Account} entities.<br>
 * Only provides a toString presenting an {@link Account} by the player's name.
 */
public class AccountConverter extends StringConverter<Account> {
        @Override
        public String toString(final Account object) {
            if (object == null) {
                return "";
            }
            return object.getName();
        }

        @Override
        public Account fromString(final String string) {
            return null;
        }
}
