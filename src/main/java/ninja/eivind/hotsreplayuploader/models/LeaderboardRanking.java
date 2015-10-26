package ninja.eivind.hotsreplayuploader.models;

import com.fasterxml.jackson.annotation.JsonProperty;

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
