package com.metacodestudio.hotsuploader.models;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.metacodestudio.testutils.DataObjectTester;

/**
 * @author ehatle
 *
 */
public class AccountTest {
	private Account account;
	private Long pid = 1234L;
	private String n = "Eivind";

	@Before
	public void setup() {
		account = new Account();
		account.setPlayerId(pid);
		account.setName(n);
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
		assertEquals(account.getPlayerId(), pid);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#setPlayerId(java.lang.Long)}.
	 */
	@Test
	public void testSetPlayerId() {
		account.setPlayerId(4321L);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals(account.getName(), n);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		account.setName("testUser");
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#getLeaderboardRankings()}.
	 */
	@Test
	public void testGetLeaderboardRankings() {
		assertNotNull(account.getLeaderboardRankings());
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.Account#setLeaderboardRankings(java.util.List)}.
	 */
	@Test
	public void testSetLeaderboardRankings() {
		account.setLeaderboardRankings(new ArrayList<LeaderboardRanking>());
	}
}
