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
	private LeaderboardRanking lr;
	private String gameMode = "HeroLeague";
	private int leagueId = 1, leagueRank = 42, currentMmr = 1337;

	@Before
	public void setup() {
		lr = new LeaderboardRanking();
		lr.setGameMode(gameMode);
		lr.setLeagueId(leagueId);
		lr.setLeagueRank(leagueRank);
		lr.setCurrentMmr(currentMmr);
	}
	@After
	public void tearDown(){
		lr = null;
	}

	@Test
	public void testClassIsValidDataObject() throws Exception {
		DataObjectTester<LeaderboardRanking> tester = new DataObjectTester<>(LeaderboardRanking.class,
				lr);
		tester.run();
	}
	
	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#LeaderboardRanking()}.
	 */
	@Test
	public void testLeaderboardRanking() {
		assertNotNull("Constructor not working", lr);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("Account.toString not providing the correct result", lr.toString(),
				("LeaderboardRanking{gameMode='" + lr.getGameMode() + '\'' + ", leagueId="
						+ lr.getLeagueId() + ", leagueRank=" + lr.getLeagueRank()
						+ ", currentMmr=" + lr.getCurrentMmr() + '}'));
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#getCurrentMmr()}.
	 */
	@Test
	public void testGetCurrentMmr() {
		assertEquals((int) currentMmr, (int) lr.getCurrentMmr());
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#setCurrentMmr(java.lang.Integer)}.
	 */
	@Test
	public void testSetCurrentMmr() {
		lr.setCurrentMmr(currentMmr+1);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#getLeagueRank()}.
	 */
	@Test
	public void testGetLeagueRank() {
		assertEquals((int) leagueRank, (int) lr.getLeagueRank());
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#setLeagueRank(java.lang.Integer)}.
	 */
	@Test
	public void testSetLeagueRank() {
		lr.setLeagueRank(leagueRank+1);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#getLeagueId()}.
	 */
	@Test
	public void testGetLeagueId() {
		assertEquals((int) leagueId, (int) lr.getLeagueId());
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#setLeagueId(java.lang.Integer)}.
	 */
	@Test
	public void testSetLeagueId() {
		lr.setLeagueId(leagueId+1);
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#getGameMode()}.
	 */
	@Test
	public void testGetGameMode() {
		assertEquals(gameMode, lr.getGameMode());
	}

	/**
	 * Test method for {@link com.metacodestudio.hotsuploader.models.LeaderboardRanking#setGameMode(java.lang.String)}.
	 */
	@Test
	public void testSetGameMode() {
		lr.setGameMode("CustomGame");
	}
}
