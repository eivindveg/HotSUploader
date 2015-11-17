package ninja.eivind.hotsreplayuploader.models;

import java.util.List;

public interface LeaderboardRanked {
    List<LeaderboardRanking> getLeaderboardRankings();

    void setLeaderboardRankings(List<LeaderboardRanking> leaderboardRankings);
}
