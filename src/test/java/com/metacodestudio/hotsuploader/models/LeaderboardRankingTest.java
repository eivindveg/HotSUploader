package com.metacodestudio.hotsuploader.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.metacodestudio.testutils.DataObjectTester;

/**
 * @author ehatle
 *
 */
public class LeaderboardRankingTest {
	private LeaderboardRanking leaderboardRanking;
	private final String GAMEMODE = "HeroLeague";
	private final int LEAGUEID = 1, LEAGUERANK = 42, CURRENTMMR = 1337;

	@Before
	public void setup() {
		leaderboardRanking = new LeaderboardRanking();
		leaderboardRanking.setGameMode(GAMEMODE);
		leaderboardRanking.setLeagueId(LEAGUEID);
		leaderboardRanking.setLeagueRank(LEAGUERANK);
		leaderboardRanking.setCurrentMmr(CURRENTMMR);
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
}
