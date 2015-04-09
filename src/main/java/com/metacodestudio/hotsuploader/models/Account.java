package com.metacodestudio.hotsuploader.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * API object retrieved from HotSLogs.com calls
 */
public class Account {

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

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(final Long playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<LeaderboardRanking> getLeaderboardRankings() {
        return leaderboardRankings;
    }

    public void setLeaderboardRankings(final List<LeaderboardRanking> leaderboardRankings) {
        this.leaderboardRankings = leaderboardRankings;
    }
}
