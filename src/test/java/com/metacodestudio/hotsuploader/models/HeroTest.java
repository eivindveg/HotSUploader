package com.metacodestudio.hotsuploader.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.metacodestudio.testutils.DataObjectTester;

/**
 * @author ehatle
 *
 */
public class HeroTest {
	// We need some mock heroes to test on. Arfas and Ufer, I choose you!
	private Hero arfas, ufer;
	private String arfas_name = "Arfas", arfas_url = "Frostmourn hungers... BLAH";
	private String ufer_name = "Ufer", ufer_url = "TRUE PALADIN OF THE LIGHT";

	/**
	 * Will run before all the test. Used for initializing variables and objects
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		arfas = new Hero(arfas_name, arfas_url);
		ufer = new Hero();
	}

	/**
	 * Runs after the tests are completed
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// Technically not necessary but will often grab the attention of the
		// garbage collector.
		arfas = null;
		arfas_name = null;
		arfas_url = null;
		ufer = null;
		ufer_name = null;
		ufer_url = null;
		// Also possible to manually run the garbage collector here
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
	 * {@link com.metacodestudio.hotsuploader.models.Hero#setPrimaryName(java.lang.String)}
	 * .
	 */
	@Test
	public void testSetPrimaryName() {
		ufer.setPrimaryName(ufer_name);
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.Hero#getPrimaryName()}.
	 */
	@Test
	public void testGetPrimaryName() {
		assertEquals("Hero.getPrimaryName not correctly setting name", arfas_name, arfas.getPrimaryName());
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.Hero#setImageURL(java.lang.String)}
	 * .
	 */
	@Test
	public void testSetImageURL() {
		ufer.setImageURL(ufer_url);
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.Hero#getImageURL()}.
	 */
	@Test
	public void testGetImageURL() {
		assertEquals("Hero.getImageURL not correctly setting url", arfas_url, arfas.getImageURL());
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.Hero#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("Hero.toString not returning primaryName", arfas.toString(), arfas_name);
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
		ufer.setPrimaryName(arfas_name);
		ufer.setImageURL(arfas_url);
		// Checking that the comparison works on content as well as object
		// references
		assertTrue("Content comparison failed", ufer.equals(arfas));
	}
}
