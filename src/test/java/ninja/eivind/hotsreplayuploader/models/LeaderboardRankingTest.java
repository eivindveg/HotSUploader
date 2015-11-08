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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import ninja.eivind.testutils.DataObjectTester;

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
	 * {@link LeaderboardRanking#LeaderboardRanking()}
	 * .
	 */
	@Test
	public void testLeaderboardRanking() {
		assertNotNull("Constructor not working", leaderboardRanking);
	}

	/**
	 * Test method for
	 * {@link LeaderboardRanking#toString()}
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
