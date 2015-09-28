package com.metacodestudio.hotsuploader.models;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.metacodestudio.testutils.DataObjectTester;

/**
 * Unit testing for Account class
 * 
 * @author ehatle
 *
 */
public class AccountTest {
	// Set of necessary variables for testing
	private Account account;
	private static final Long PLAYERID = 1234L;
	private static final String PLAYER_NAME = "Eivind";

	@Before
	public void setup() {
		// Initializing variables
		account = new Account();
		account.setPlayerId(PLAYERID);
		account.setName(PLAYER_NAME);
	}

	@Test
	public void testClassIsValidDataObject() throws Exception {
		// Verifying that the Account object is an actual object and contains
		// all neccessary getters and setters
		DataObjectTester<Account> tester = new DataObjectTester<>(Account.class, account);
		tester.run();
	}

	@Test
	public void testToString() {
		// Testing that toString is working properly. Testing against final
		// variables
		assertEquals("Account.toString not providing the correct result", account.toString(),
				("Account{playerId='" + account.getPlayerId() + '\'' + ", name='" + account.getName() + '\''
						+ ", leaderboardRankings=" + account.getLeaderboardRankings() + '}'));
	}
}
