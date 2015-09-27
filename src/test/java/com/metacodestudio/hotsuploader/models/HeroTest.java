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
	private Hero arfas, ufer;
	private String arfas_name = "Arfas", aurl="Frostmourn hungers... BLAH";
	private String ufer_name = "Ufer", uurl="TRUE PALADIN OF THE LIGHT";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		arfas = new Hero(arfas_name, aurl);
		ufer = new Hero();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		arfas = null;
		arfas_name = null;
		aurl = null;
		ufer_name = null;
		uurl = null;
	}

	@Test
	public void testClassIsValidDataObject() throws Exception {
		DataObjectTester<Hero> tester = new DataObjectTester<>(Hero.class,
				arfas);
		tester.run();
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Hero#Hero()}.
	 */
	@Test
	public void testHero() {
		assertNotNull("Hero.constructor() not working", ufer);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Hero#Hero(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHeroStringString() {
		assertNotNull("Hero.constructor(String, String) not working", arfas);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Hero#setPrimaryName(java.lang.String)}.
	 */
	@Test
	public void testSetPrimaryName() {
		ufer.setPrimaryName(ufer_name);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Hero#getPrimaryName()}.
	 */
	@Test
	public void testGetPrimaryName() {
		assertEquals("Hero.getPrimaryName not correctly setting name", arfas_name, arfas.getPrimaryName());
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Hero#setImageURL(java.lang.String)}.
	 */
	@Test
	public void testSetImageURL() {
		ufer.setImageURL(uurl);
	}
	
	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Hero#getImageURL()}.
	 */
	@Test
	public void testGetImageURL() {
		assertEquals("Hero.getImageURL not correctly setting url", aurl, arfas.getImageURL());
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Hero#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("Hero.toString not returning primaryName", arfas.toString(), arfas_name);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Hero#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertFalse("False comparison failed", arfas.equals(ufer));
		assertTrue("True comparison failed", (ufer.equals(ufer) && arfas.equals(arfas)));
		ufer.setPrimaryName(arfas_name);
		ufer.setImageURL(aurl);
		assertTrue("Content comparison failed", ufer.equals(arfas));
	}
}
