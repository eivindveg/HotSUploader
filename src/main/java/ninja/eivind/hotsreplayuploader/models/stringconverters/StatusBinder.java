package ninja.eivind.hotsreplayuploader.models.stringconverters;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.inject.Singleton;

/**
 * @author Eivind Vegsundvåg
 */
@Singleton
public class StatusBinder {

    private final StringProperty message = new SimpleStringProperty();

    public StringProperty message() {
        return message;
    }
}
