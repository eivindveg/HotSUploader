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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * API object retrieved from HotSLogs.com calls, which represents a player's battle net account.<br>
 * Used to retrieve the player's leaderboard rankings.
 */
public class Account implements LeaderboardRanked, Named, Player {

    @JsonProperty("PlayerID")
    private Long playerId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("LeaderboardRankings")
    private List<LeaderboardRanking> leaderboardRankings = new ArrayList<>();

    @Override
    public String toString() {
        return "Account{" +
                "playerId='" + playerId + '\'' +
                ", name='" + name + '\'' +
                ", leaderboardRankings=" + leaderboardRankings +
                '}';
    }

    /**
     * Returns the HotSLogs.com player id to use when building URLs to open the browser to this account's profile
     *
     * @return this account's player id
     */
    @Override
    public Long getPlayerId() {
        return playerId;
    }

    @Override
    public void setPlayerId(final Long playerId) {
        this.playerId = playerId;
    }

    /**
     * Gets the player name associated with this HotSLogs.com account, as represented by this player's battletag
     *
     * @return this account's name
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets a list containing the various {@link LeaderboardRanking}s this player has achieved on HotSLogs.com
     *
     * @return a list of type {@link LeaderboardRanking}
     * @see LeaderboardRanking
     */
    @Override
    public List<LeaderboardRanking> getLeaderboardRankings() {
        return leaderboardRankings;
    }

    /**
     * Replace the list of {@link LeaderboardRanking}s this player has achieved on HotSLogs.com. As this is a simple
     * data object representation of data retrieved from HotSLogs.com, this has absolutely no effect.
     *
     * @param leaderboardRankings a list of type {@link LeaderboardRanking}
     */
    @Override
    public void setLeaderboardRankings(final List<LeaderboardRanking> leaderboardRankings) {
        this.leaderboardRankings = leaderboardRankings;
    }
}
