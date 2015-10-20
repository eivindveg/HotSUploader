package com.metacodestudio.hotsuploader.models;

import com.metacodestudio.testutils.DataObjectTester;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ehatle
 */
public class HeroTest {
    private static final String ARFAS_NAME = "Arfas", ARFAS_URL = "Frostmourn hungers... BLAH";
    // We need some mock heroes to test on. Arfas and Ufer, I choose you!
    private Hero arfas, ufer;
    private String UFER_NAME = "Ufer", UFER_URL = "TRUE PALADIN OF THE LIGHT";

    /**
     * Will run before all the test. Used for initializing variables and objects
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        arfas = new Hero(ARFAS_NAME, ARFAS_URL);
        ufer = new Hero();
        ufer.setPrimaryName(UFER_NAME);
        ufer.setImageURL(UFER_URL);
    }

    @Test
    public void testClassIsValidDataObject() throws Exception {
        // Verifying that the Hero object is an actual object and contains all
        // Necessary getters and setters
        DataObjectTester<Hero> tester = new DataObjectTester<>(Hero.class, arfas);
        tester.run();
    }

    /**
     * Test method for
     * {@link com.metacodestudio.hotsuploader.models.Hero#Hero()}.
     */
    @Test
    public void testHero() {
        assertNotNull("Hero.constructor() not working", ufer);
    }

    /**
     * Test method for
     * {@link com.metacodestudio.hotsuploader.models.Hero#Hero(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testHeroStringString() {
        assertNotNull("Hero.constructor(String, String) not working", arfas);
    }

    /**
     * Test method for
     * {@link com.metacodestudio.hotsuploader.models.Hero#toString()}.
     */
    @Test
    public void testToString() {
        assertEquals("Hero.toString not returning primaryName", arfas.toString(), ARFAS_NAME);
    }

    /**
     * Test method for
     * {@link com.metacodestudio.hotsuploader.models.Hero#equals(java.lang.Object)}
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
