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

/**
 * API object retrieved from HotSLogs.com calls, included in a {@link Account} object.<br>
 * Can represent several rankings, that may describe player skill, like MMR, league and rank.
 */
public class LeaderboardRanking {

    @JsonProperty("GameMode")
    private String gameMode;
    @JsonProperty("LeagueID")
    private Integer leagueId;
    @JsonProperty("LeagueRank")
    private Integer leagueRank;
    @JsonProperty("CurrentMMR")
    private Integer currentMmr;

    public LeaderboardRanking() {
    }

    @Override
    public String toString() {
        return "LeaderboardRanking{" +
                "gameMode='" + gameMode + '\'' +
                ", leagueId=" + leagueId +
                ", leagueRank=" + leagueRank +
                ", currentMmr=" + currentMmr +
                '}';
    }

    public Integer getCurrentMmr() {
        return currentMmr;
    }

    public void setCurrentMmr(final Integer currentMmr) {
        this.currentMmr = currentMmr;
    }

    public Integer getLeagueRank() {
        return leagueRank;
    }

    public void setLeagueRank(final Integer leagueRank) {
        this.leagueRank = leagueRank;
    }

    public Integer getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(final Integer leagueId) {
        this.leagueId = leagueId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(final String gameMode) {
        this.gameMode = gameMode;
    }
}
