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

import ninja.eivind.hotsreplayuploader.providers.hotslogs.HotSLogsHero;
import ninja.eivind.testutils.DataObjectTester;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HotSLogsHeroTest {
    private static final String ARFAS_NAME = "Arfas", ARFAS_URL = "Frostmourn hungers... BLAH";
    // We need some mock heroes to test on. Arfas and Ufer, I choose you!
    private HotSLogsHero arfas, ufer;
    private String UFER_NAME = "Ufer", UFER_URL = "TRUE PALADIN OF THE LIGHT";

    /**
     * Will run before all the test. Used for initializing variables and objects
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        arfas = new HotSLogsHero(ARFAS_NAME, ARFAS_URL);
        ufer = new HotSLogsHero();
        ufer.setPrimaryName(UFER_NAME);
        ufer.setImageURL(UFER_URL);
    }

    @Test
    public void testClassIsValidDataObject() throws Exception {
        // Verifying that the Hero object is an actual object and contains all
        // Necessary getters and setters
        DataObjectTester<HotSLogsHero> tester = new DataObjectTester<>(HotSLogsHero.class, arfas);
        tester.run();
    }

    /**
     * Test method for
     * {@link HotSLogsHero#HotSLogsHero()}.
     */
    @Test
    public void testHero() {
        assertNotNull("Hero.constructor() not working", ufer);
    }

    /**
     * Test method for
     * {@link HotSLogsHero#HotSLogsHero(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testHeroStringString() {
        assertNotNull("Hero.constructor(String, String) not working", arfas);
    }

    /**
     * Test method for
     * {@link HotSLogsHero#toString()}.
     */
    @Test
    public void testToString() {
        assertEquals("Hero.toString not returning primaryName", arfas.toString(), ARFAS_NAME);
    }

    /**
     * Test method for
     * {@link HotSLogsHero#equals(java.lang.Object)}
     * .
     */
    @Test
    public void testEqualsObject() {
        // Check that it recognizes two different objects
        assertFalse("False comparison failed", arfas.equals(ufer));
        // Check that it recognizes the same objects
        assertTrue("True comparison failed", (ufer.equals(ufer) && arfas.equals(arfas)));
        // Updating content of ufer to match arfas
        ufer.setPrimaryName(ARFAS_NAME);
        ufer.setImageURL(ARFAS_URL);
        // Checking that the comparison works on content as well as object
        // references
        assertTrue("Content comparison failed", ufer.equals(arfas));
    }
}
