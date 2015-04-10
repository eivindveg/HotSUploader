package com.metacodestudio.hotsuploader.models.stringconverters;

import com.metacodestudio.hotsuploader.models.Hero;
import javafx.util.StringConverter;

public class HeroConverter extends StringConverter<Hero> {

    @Override
    public String toString(final Hero hero) {
        if(hero == null) {
            return "";
        }
        return hero.getPrimaryName();
    }

    @Override
    public Hero fromString(final String primaryName) {
        final String imageURL = primaryName.replaceAll("[\\W_]", "");
        System.out.println(imageURL);
        if (imageURL.equals("")) {
            return null;
        }

        return new Hero(primaryName, imageURL);
    }
}
