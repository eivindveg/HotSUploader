package com.metacodestudio.hotsuploader.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.metacodestudio.testutils.DataObjectTester;

/**
 * @author ehatle
 *
 */
public class LeaderboardRankingTest {
	private LeaderboardRanking leaderboardRanking;
	private String gameMode = "HeroLeague";
	private int leagueId = 1, leagueRank = 42, currentMmr = 1337;

	@Before
	public void setup() {
		leaderboardRanking = new LeaderboardRanking();
		leaderboardRanking.setGameMode(gameMode);
		leaderboardRanking.setLeagueId(leagueId);
		leaderboardRanking.setLeagueRank(leagueRank);
		leaderboardRanking.setCurrentMmr(currentMmr);
	}

	@After
	public void tearDown() {
		// Technically not necessary but will often grab the attention of the
		// garbage collector.
		leaderboardRanking = null;
		// Also possible to manually run the garbage collector here
	}

	@Test
	public void testClassIsValidDataObject() throws Exception {
		// Verifying that the LeaderboardRanking object is an actual object and
		// contains all necessary getters and setters
		DataObjectTester<LeaderboardRanking> tester = new DataObjectTester<>(LeaderboardRanking.class,
				leaderboardRanking);
		tester.run();
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#LeaderboardRanking()}
	 * .
	 */
	@Test
	public void testLeaderboardRanking() {
		assertNotNull("Constructor not working", leaderboardRanking);
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#toString()}
	 * .
	 */
	@Test
	public void testToString() {
		// Testing that toString is working properly. Testing against final
		// variables
		assertEquals("Account.toString not providing the correct result", leaderboardRanking.toString(),
				("LeaderboardRanking{gameMode='" + leaderboardRanking.getGameMode() + '\'' + ", leagueId="
						+ leaderboardRanking.getLeagueId() + ", leagueRank=" + leaderboardRanking.getLeagueRank()
						+ ", currentMmr=" + leaderboardRanking.getCurrentMmr() + '}'));
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#getCurrentMmr()}
	 * .
	 */
	@Test
	public void testGetCurrentMmr() {
		assertEquals((int) currentMmr, (int) leaderboardRanking.getCurrentMmr());
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#setCurrentMmr(java.lang.Integer)}
	 * .
	 */
	@Test
	public void testSetCurrentMmr() {
		leaderboardRanking.setCurrentMmr(currentMmr + 1);
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#getLeagueRank()}
	 * .
	 */
	@Test
	public void testGetLeagueRank() {
		assertEquals((int) leagueRank, (int) leaderboardRanking.getLeagueRank());
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#setLeagueRank(java.lang.Integer)}
	 * .
	 */
	@Test
	public void testSetLeagueRank() {
		leaderboardRanking.setLeagueRank(leagueRank + 1);
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#getLeagueId()}
	 * .
	 */
	@Test
	public void testGetLeagueId() {
		assertEquals((int) leagueId, (int) leaderboardRanking.getLeagueId());
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#setLeagueId(java.lang.Integer)}
	 * .
	 */
	@Test
	public void testSetLeagueId() {
		leaderboardRanking.setLeagueId(leagueId + 1);
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#getGameMode()}
	 * .
	 */
	@Test
	public void testGetGameMode() {
		assertEquals(gameMode, leaderboardRanking.getGameMode());
	}

	/**
	 * Test method for
	 * {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#setGameMode(java.lang.String)}
	 * .
	 */
	@Test
	public void testSetGameMode() {
		leaderboardRanking.setGameMode("CustomGame");
	}
}
