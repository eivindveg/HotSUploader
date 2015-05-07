package com.metacodestudio.hotsuploader.models.stringconverters;

import com.metacodestudio.hotsuploader.models.Hero;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestHeroConverter {

    private HeroConverter heroConverter;
    private Hero hero;

    @Before
    public void setup() {
        heroConverter = new HeroConverter();
        hero = new Hero();
        hero.setImageURL("TestHero");
        hero.setPrimaryName("Test Hero'");
    }

    @Test
    public void testToString() {
        String expected = hero.getBoxValue();
        String actual = heroConverter.toString(hero);
        assertEquals("HeroConverter builds proper String representation of hero", expected, actual);

        expected = "";
        actual = heroConverter.toString(null);
        assertEquals("HeroConverter can handle null values", expected, actual);
    }

    @Test
    public void testFromString() {
        Hero expected = hero;
        Hero actual = heroConverter.fromString("Test Hero'");

        assertEquals("HeroConverter creates a hero from a string", expected, actual);
        assertEquals("HeroConverter knows how to build an ImageURL", expected.getImageURL(), actual.getImageURL());
    }
}
