package com.amigos.courtpulse.mapper;

import com.amigos.courtpulse.dto.analytics.ClubCompletedTournamentResponse;
import com.amigos.courtpulse.dto.analytics.HeadToHeadResponse;
import com.amigos.courtpulse.dto.analytics.LiveMatchPlayerResponse;
import com.amigos.courtpulse.dto.analytics.PartnerSummaryResponse;
import com.amigos.courtpulse.dto.analytics.PartnershipStatsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerClubAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerLifetimeAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.PlayerTournamentAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.TournamentSummaryResponse;
import com.amigos.courtpulse.dto.analytics.TournamentWinPreviewResponse;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.entity.PlayerClubStats;
import com.amigos.courtpulse.entity.PlayerLifetimeStats;
import com.amigos.courtpulse.entity.PlayerTournamentStats;
import com.amigos.courtpulse.entity.TournamentSummary;
import com.amigos.courtpulse.util.ObjectUtil;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PlayerAnalyticsMapper {

    public PlayerLifetimeAnalyticsResponse toLifetimeResponse(
            Player player,
            PlayerLifetimeStats stats,
            PartnerSummaryResponse bestDoublesPartner,
            List<TournamentWinPreviewResponse> recentTournamentWins
    ) {
        int matchesWon = valueOrZero(stats, PlayerLifetimeStats::getMatchesWon);
        int matchesLost = valueOrZero(stats, PlayerLifetimeStats::getMatchesLost);
        int matchesPlayed = valueOrZero(stats, PlayerLifetimeStats::getMatchesPlayed);
        int totalPointsScored = valueOrZero(stats, PlayerLifetimeStats::getTotalPointsScored);
        int totalPointsConceded = valueOrZero(stats, PlayerLifetimeStats::getTotalPointsConceded);

        return new PlayerLifetimeAnalyticsResponse(
                player.getPlayerCode(),
                valueOrZero(stats, PlayerLifetimeStats::getTournamentsPlayed),
                matchesPlayed,
                matchesWon,
                matchesLost,
                valueOrZero(stats, PlayerLifetimeStats::getMatchesDrawn),
                computeWinRatio(matchesWon, matchesLost),
                valueOrZero(stats, PlayerLifetimeStats::getSinglesMatchesPlayed),
                valueOrZero(stats, PlayerLifetimeStats::getSinglesMatchesWon),
                valueOrZero(stats, PlayerLifetimeStats::getSinglesMatchesLost),
                valueOrZero(stats, PlayerLifetimeStats::getDoublesMatchesPlayed),
                valueOrZero(stats, PlayerLifetimeStats::getDoublesMatchesWon),
                valueOrZero(stats, PlayerLifetimeStats::getDoublesMatchesLost),
                valueOrZero(stats, PlayerLifetimeStats::getLeagueMatchesPlayed),
                valueOrZero(stats, PlayerLifetimeStats::getLeagueMatchesWon),
                valueOrZero(stats, PlayerLifetimeStats::getKnockoutMatchesPlayed),
                valueOrZero(stats, PlayerLifetimeStats::getKnockoutMatchesWon),
                totalPointsScored,
                totalPointsConceded,
                totalPointsScored - totalPointsConceded,
                computeAverage(totalPointsScored, matchesPlayed),
                valueOrZero(stats, PlayerLifetimeStats::getTournamentWins),
                valueOrZero(stats, PlayerLifetimeStats::getTournamentRunnerUp),
                valueOrZero(stats, PlayerLifetimeStats::getDominantMatchWins),
                valueOrZero(stats, PlayerLifetimeStats::getCurrentWinStreak),
                valueOrZero(stats, PlayerLifetimeStats::getBestWinStreak),
                stats == null ? null : stats.getLastMatchAt(),
                bestDoublesPartner,
                recentTournamentWins
        );
    }

    public PlayerTournamentAnalyticsResponse toTournamentResponse(PlayerTournamentStats stats) {
        return new PlayerTournamentAnalyticsResponse(
                stats.getTournament().getId(),
                stats.getTournament().getTournamentCode(),
                stats.getTournament().getTournamentName(),
                stats.getTournament().getTournamentDate(),
                stats.getMatchesPlayed(),
                stats.getMatchesWon(),
                stats.getMatchesLost(),
                stats.getMatchesDrawn(),
                stats.getPointsScored(),
                stats.getPointsConceded(),
                stats.getBestRoundReached(),
                stats.getTournamentPlacement()
        );
    }

    public TournamentWinPreviewResponse toTournamentWinPreview(PlayerTournamentStats stats) {
        return new TournamentWinPreviewResponse(
                stats.getTournament().getId(),
                stats.getTournament().getTournamentCode(),
                stats.getTournament().getTournamentName(),
                stats.getTournament().getTournamentDate()
        );
    }

    public PlayerClubAnalyticsResponse toClubResponse(
            PlayerClubStats stats,
            List<ClubCompletedTournamentResponse> recentCompletedTournaments
    ) {
        return new PlayerClubAnalyticsResponse(
                stats.getPlayer().getPlayerCode(),
                stats.getClub().getClubCode(),
                stats.getClub().getClubName(),
                stats.getMatchesPlayed(),
                stats.getMatchesWon(),
                stats.getMatchesLost(),
                stats.getMatchesDrawn(),
                computeWinRatio(stats.getMatchesWon(), stats.getMatchesLost()),
                stats.getTotalPointsScored(),
                stats.getTotalPointsConceded(),
                stats.getTotalPointsScored() - stats.getTotalPointsConceded(),
                stats.getTournamentWins(),
                stats.getTournamentRunnerUp(),
                stats.getLastMatchAt(),
                recentCompletedTournaments
        );
    }

    public ClubCompletedTournamentResponse toClubCompletedTournamentResponse(TournamentSummary summary) {
        Player champion = summary.getChampionPlayer();
        return new ClubCompletedTournamentResponse(
                summary.getTournamentId(),
                summary.getTournament().getTournamentCode(),
                summary.getTournament().getTournamentName(),
                summary.getTournament().getTournamentDate(),
                champion == null ? null : champion.getPlayerCode(),
                champion == null ? null : champion.getName(),
                summary.getParticipantCount()
        );
    }

    public TournamentSummaryResponse toTournamentSummaryResponse(TournamentSummary summary) {
        Player champion = summary.getChampionPlayer();
        Player runnerUp = summary.getRunnerUpPlayer();
        return new TournamentSummaryResponse(
                summary.getTournamentId(),
                summary.getTournament().getTournamentCode(),
                summary.getTournament().getTournamentName(),
                summary.getTournament().getTournamentDate(),
                summary.getTotalMatchesPlayed(),
                summary.getTotalPointsScored(),
                summary.getParticipantCount(),
                champion == null ? null : champion.getPlayerCode(),
                champion == null ? null : champion.getName(),
                runnerUp == null ? null : runnerUp.getPlayerCode(),
                runnerUp == null ? null : runnerUp.getName(),
                summary.getStatus()
        );
    }

    public PartnerSummaryResponse toPartnerSummary(
            Player partner,
            int matchesTogether,
            int winsTogether
    ) {
        return new PartnerSummaryResponse(
                partner.getPlayerCode(),
                partner.getName(),
                partner.getGameName(),
                matchesTogether,
                winsTogether,
                computeWinRatio(winsTogether, matchesTogether - winsTogether)
        );
    }

    public LiveMatchPlayerResponse toLiveMatchPlayerResponse(Player player, PlayerTournamentStats stats) {
        if (ObjectUtil.isNull(stats)) {
            return new LiveMatchPlayerResponse(
                    player.getPlayerCode(),
                    player.getName(),
                    player.getGameName(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    null,
                    null,
                    null
            );
        }
        return new LiveMatchPlayerResponse(
                player.getPlayerCode(),
                player.getName(),
                player.getGameName(),
                stats.getMatchesPlayed(),
                stats.getMatchesWon(),
                stats.getMatchesLost(),
                stats.getMatchesDrawn(),
                stats.getPointsScored(),
                stats.getPointsConceded(),
                computeWinRatio(stats.getMatchesWon(), stats.getMatchesLost()),
                stats.getBestRoundReached(),
                stats.getTournamentPlacement()
        );
    }

    public PartnershipStatsResponse toPartnershipStats(int matchesTogether, int winsTogether) {
        return new PartnershipStatsResponse(
                matchesTogether,
                winsTogether,
                computeWinRatio(winsTogether, matchesTogether - winsTogether)
        );
    }

    public HeadToHeadResponse toHeadToHead(int matchesPlayed, int teamAWins, int teamBWins, int draws) {
        return new HeadToHeadResponse(matchesPlayed, teamAWins, teamBWins, draws);
    }

    public HeadToHeadResponse emptyHeadToHead() {
        return new HeadToHeadResponse(0, 0, 0, 0);
    }

    public PartnershipStatsResponse emptyPartnershipStats() {
        return new PartnershipStatsResponse(0, 0, null);
    }

    private Double computeWinRatio(int wins, int losses) {
        int decisiveMatches = wins + losses;
        if (decisiveMatches == 0) {
            return null;
        }
        return (double) wins / decisiveMatches;
    }

    private Double computeAverage(int total, int count) {
        if (count == 0) {
            return null;
        }
        return (double) total / count;
    }

    private int valueOrZero(PlayerLifetimeStats stats, java.util.function.Function<PlayerLifetimeStats, Integer> getter) {
        if (ObjectUtil.isNull(stats)) {
            return 0;
        }
        return getter.apply(stats);
    }
}
