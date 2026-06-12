package com.amigos.courtpulse.util;

import com.amigos.courtpulse.enums.RoundNameEnum;
import com.amigos.courtpulse.enums.TournamentTypeEnum;

public final class AnalyticsUtil {

    private static final int DOMINANT_MATCH_MARGIN = 10;
    private static final int MIN_PARTNER_MATCHES = 3;
    private static final int RECENT_TOURNAMENT_WINS_PREVIEW_LIMIT = 5;
    private static final int RECENT_CLUB_COMPLETED_TOURNAMENTS_LIMIT = 5;
    private static final int DEFAULT_TOURNAMENT_HISTORY_LIMIT = 20;
    private static final int MAX_TOURNAMENT_HISTORY_LIMIT = 50;

    private AnalyticsUtil() {
    }

    public static int minPartnerMatches() {
        return MIN_PARTNER_MATCHES;
    }

    public static int recentTournamentWinsPreviewLimit() {
        return RECENT_TOURNAMENT_WINS_PREVIEW_LIMIT;
    }

    public static int recentClubCompletedTournamentsLimit() {
        return RECENT_CLUB_COMPLETED_TOURNAMENTS_LIMIT;
    }

    public static int defaultTournamentHistoryLimit() {
        return DEFAULT_TOURNAMENT_HISTORY_LIMIT;
    }

    public static int maxTournamentHistoryLimit() {
        return MAX_TOURNAMENT_HISTORY_LIMIT;
    }

    public static int clampTournamentHistoryLimit(int requested) {
        if (requested <= 0) {
            return DEFAULT_TOURNAMENT_HISTORY_LIMIT;
        }
        return Math.min(requested, MAX_TOURNAMENT_HISTORY_LIMIT);
    }

    public static boolean isKnockoutRound(RoundNameEnum roundName) {
        return roundName == RoundNameEnum.QUARTER_FINAL
                || roundName == RoundNameEnum.SEMI_FINAL
                || roundName == RoundNameEnum.FINAL;
    }

    public static boolean isDoublesTournament(TournamentTypeEnum tournamentType) {
        return tournamentType == TournamentTypeEnum.FIXED_DOUBLES
                || tournamentType == TournamentTypeEnum.RANDOM_DOUBLES;
    }

    public static int roundOrder(RoundNameEnum roundName) {
        return switch (roundName) {
            case LEAGUE_STAGE -> 1;
            case QUARTER_FINAL -> 2;
            case SEMI_FINAL -> 3;
            case FINAL -> 4;
        };
    }

    public static boolean isHigherRound(RoundNameEnum candidate, RoundNameEnum current) {
        if (ObjectUtil.isNull(current)) {
            return true;
        }
        return roundOrder(candidate) > roundOrder(current);
    }

    public static boolean isDominantWin(int scoreA, int scoreB) {
        return Math.abs(scoreA - scoreB) >= DOMINANT_MATCH_MARGIN;
    }
}
