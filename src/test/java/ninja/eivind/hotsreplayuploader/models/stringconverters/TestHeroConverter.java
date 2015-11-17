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

import ninja.eivind.hotsreplayuploader.providers.hotslogs.HotSLogsHero;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestHeroConverter {

    private HeroConverter heroConverter;
    private HotSLogsHero hero;

    @Before
    public void setup() {
        heroConverter = new HeroConverter();
        hero = new HotSLogsHero();
        hero.setImageURL("TestHero");
        hero.setPrimaryName("Test Hero'");
    }

    @Test
    public void testToString() {
        String expected = hero.getPrimaryName();
        String actual = heroConverter.toString(hero);
        assertEquals("HeroConverter builds proper String representation of hero", expected, actual);

        expected = "";
        actual = heroConverter.toString(null);
        assertEquals("HeroConverter can handle null values", expected, actual);
    }

    @Test
    public void testFromString() {
        HotSLogsHero expected = hero;
        HotSLogsHero actual = heroConverter.fromString("Test Hero'");

        assertEquals("HeroConverter creates a hero from a string", expected, actual);
        assertEquals("HeroConverter knows how to build an ImageURL", expected.getImageURL(), actual.getImageURL());
        assertNull("HeroConverter returns null when receiving an empty string", heroConverter.fromString(""));
    }
}
