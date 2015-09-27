package com.metacodestudio.hotsuploader.models;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.metacodestudio.testutils.DataObjectTester;

/**
 * @author emillh
 *
 */
public class AccountTest {

	private Account account;

	@Before
	public void setup() {
		account = new Account();
		account.setPlayerId(1234L);
		account.setName("TestUser");
	}

	@After
	public void teardown() {
		account = null;
	}

	@Test
	public void testClassIsValidDataObject() throws Exception {
		DataObjectTester<Account> tester = new DataObjectTester<>(Account.class, account);
		tester.run();
	}

	@Test
	public void testToString() {
		assertEquals("Account.toString not providing the correct result", account.toString(),
				("Account{playerId='" + account.getPlayerId() + '\'' + ", name='" + account.getName() + '\''
						+ ", leaderboardRankings=" + account.getLeaderboardRankings() + '}'));
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#getPlayerId()}.
	 */
	@Test
	public void testGetPlayerId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#setPlayerId(java.lang.Long)}.
	 */
	@Test
	public void testSetPlayerId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#getName()}.
	 */
	@Test
	public void testGetName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#getLeaderboardRankings()}.
	 */
	@Test
	public void testGetLeaderboardRankings() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#setLeaderboardRankings(java.util.List)}.
	 */
	@Test
	public void testSetLeaderboardRankings() {
		fail("Not yet implemented"); // TODO
	}

}
