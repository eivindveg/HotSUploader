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
