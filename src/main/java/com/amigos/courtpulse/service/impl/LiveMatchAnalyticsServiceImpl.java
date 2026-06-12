package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.dto.analytics.HeadToHeadResponse;
import com.amigos.courtpulse.dto.analytics.LiveMatchAnalyticsRequest;
import com.amigos.courtpulse.dto.analytics.LiveMatchAnalyticsResponse;
import com.amigos.courtpulse.dto.analytics.LiveMatchPlayerResponse;
import com.amigos.courtpulse.dto.analytics.LiveMatchTeamResponse;
import com.amigos.courtpulse.dto.analytics.PartnershipStatsResponse;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.entity.PlayerTournamentStats;
import com.amigos.courtpulse.entity.TournamentMetadata;
import com.amigos.courtpulse.enums.TournamentTypeEnum;
import com.amigos.courtpulse.exception.PlayerNotFoundException;
import com.amigos.courtpulse.exception.TournamentNotFoundException;
import com.amigos.courtpulse.mapper.PlayerAnalyticsMapper;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.repository.PlayerTournamentStatsRepository;
import com.amigos.courtpulse.repository.TournamentMatchPlayerRepository;
import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import com.amigos.courtpulse.service.LiveMatchAnalyticsService;
import com.amigos.courtpulse.util.AnalyticsUtil;
import com.amigos.courtpulse.util.ObjectUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LiveMatchAnalyticsServiceImpl implements LiveMatchAnalyticsService {

    private final TournamentMetadataRepository tournamentMetadataRepository;
    private final PlayerRepository playerRepository;
    private final PlayerTournamentStatsRepository playerTournamentStatsRepository;
    private final TournamentMatchPlayerRepository tournamentMatchPlayerRepository;
    private final PlayerAnalyticsMapper playerAnalyticsMapper;

    @Override
    @Transactional(readOnly = true)
    public LiveMatchAnalyticsResponse getLiveMatchAnalytics(Long tournamentId, LiveMatchAnalyticsRequest request) {
        TournamentMetadata tournament = tournamentMetadataRepository.findByIdWithClub(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        List<String> teamACodes = normalizeCodes(request.teamAPlayerCodes());
        List<String> teamBCodes = normalizeCodes(request.teamBPlayerCodes());
        validateTeamSizes(tournament.getTournamentType(), teamACodes.size(), teamBCodes.size());
        validateDistinctCodes(teamACodes, teamBCodes);

        List<String> allCodes = new ArrayList<>(teamACodes.size() + teamBCodes.size());
        allCodes.addAll(teamACodes);
        allCodes.addAll(teamBCodes);

        Map<String, Player> playersByCode = playerRepository.findByPlayerCodeIn(allCodes).stream()
                .collect(Collectors.toMap(
                        player -> player.getPlayerCode().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        ensureAllPlayersFound(allCodes, playersByCode);

        List<Long> allPlayerIds = allCodes.stream()
                .map(code -> playersByCode.get(code).getId())
                .toList();

        Map<Long, PlayerTournamentStats> statsByPlayerId = playerTournamentStatsRepository
                .findByPlayerIdsAndTournamentId(allPlayerIds, tournamentId)
                .stream()
                .collect(Collectors.toMap(stats -> stats.getPlayer().getId(), Function.identity()));

        boolean doubles = AnalyticsUtil.isDoublesTournament(tournament.getTournamentType());
        HeadToHeadResponse headToHead = doubles
                ? resolveDoublesHeadToHead(teamACodes, teamBCodes, playersByCode)
                : resolveSinglesHeadToHead(teamACodes.getFirst(), teamBCodes.getFirst(), playersByCode);

        LiveMatchTeamResponse teamA = buildTeamResponse(teamACodes, playersByCode, statsByPlayerId, doubles);
        LiveMatchTeamResponse teamB = buildTeamResponse(teamBCodes, playersByCode, statsByPlayerId, doubles);

        return new LiveMatchAnalyticsResponse(
                tournament.getId(),
                tournament.getTournamentCode(),
                tournament.getTournamentType(),
                tournament.getClub().getId(),
                tournament.getClub().getClubCode(),
                teamA,
                teamB,
                headToHead
        );
    }

    private List<String> normalizeCodes(List<String> codes) {
        return codes.stream()
                .map(code -> code.toUpperCase(Locale.ROOT).trim())
                .toList();
    }

    private void validateTeamSizes(TournamentTypeEnum tournamentType, int teamASize, int teamBSize) {
        int expectedSize = AnalyticsUtil.isDoublesTournament(tournamentType) ? 2 : 1;
        if (ObjectUtil.isNotEqual(teamASize, expectedSize) || ObjectUtil.isNotEqual(teamBSize, expectedSize)) {
            throw new IllegalArgumentException(
                    "Team size does not match tournament type " + tournamentType
                            + ". Expected " + expectedSize + " player(s) per team.");
        }
    }

    private void validateDistinctCodes(List<String> teamACodes, List<String> teamBCodes) {
        Set<String> seen = new HashSet<>();
        for (String code : teamACodes) {
            if (!seen.add(code)) {
                throw new IllegalArgumentException("Duplicate player code in request: " + code);
            }
        }
        for (String code : teamBCodes) {
            if (!seen.add(code)) {
                throw new IllegalArgumentException("Duplicate player code in request: " + code);
            }
        }
    }

    private void ensureAllPlayersFound(List<String> requestedCodes, Map<String, Player> playersByCode) {
        for (String code : requestedCodes) {
            if (!playersByCode.containsKey(code)) {
                throw new PlayerNotFoundException(code);
            }
        }
    }

    private LiveMatchTeamResponse buildTeamResponse(
            List<String> playerCodes,
            Map<String, Player> playersByCode,
            Map<Long, PlayerTournamentStats> statsByPlayerId,
            boolean doubles
    ) {
        List<LiveMatchPlayerResponse> players = playerCodes.stream()
                .map(code -> {
                    Player player = playersByCode.get(code);
                    PlayerTournamentStats stats = statsByPlayerId.get(player.getId());
                    return playerAnalyticsMapper.toLiveMatchPlayerResponse(player, stats);
                })
                .toList();

        PartnershipStatsResponse partnership = null;
        if (doubles) {
            Player playerOne = playersByCode.get(playerCodes.get(0));
            Player playerTwo = playersByCode.get(playerCodes.get(1));
            partnership = resolvePartnershipStats(playerOne.getId(), playerTwo.getId());
        }

        return new LiveMatchTeamResponse(players, partnership);
    }

    private HeadToHeadResponse resolveSinglesHeadToHead(
            String teamAPlayerCode,
            String teamBPlayerCode,
            Map<String, Player> playersByCode
    ) {
        Long teamAPlayerId = playersByCode.get(teamAPlayerCode).getId();
        Long teamBPlayerId = playersByCode.get(teamBPlayerCode).getId();
        List<Object[]> results = tournamentMatchPlayerRepository.findSinglesHeadToHead(teamAPlayerId, teamBPlayerId);
        return mapHeadToHead(results);
    }

    private HeadToHeadResponse resolveDoublesHeadToHead(
            List<String> teamACodes,
            List<String> teamBCodes,
            Map<String, Player> playersByCode
    ) {
        Long teamAOneId = playersByCode.get(teamACodes.get(0)).getId();
        Long teamATwoId = playersByCode.get(teamACodes.get(1)).getId();
        Long teamBOneId = playersByCode.get(teamBCodes.get(0)).getId();
        Long teamBTwoId = playersByCode.get(teamBCodes.get(1)).getId();
        List<Object[]> results = tournamentMatchPlayerRepository.findDoublesHeadToHead(
                teamAOneId, teamATwoId, teamBOneId, teamBTwoId);
        return mapHeadToHead(results);
    }

    private PartnershipStatsResponse resolvePartnershipStats(Long playerOneId, Long playerTwoId) {
        List<Object[]> results = tournamentMatchPlayerRepository.findPartnershipStats(playerOneId, playerTwoId);
        if (results.isEmpty() || ObjectUtil.isNull(results.getFirst()[0])) {
            return playerAnalyticsMapper.emptyPartnershipStats();
        }
        Object[] row = results.getFirst();
        int matchesTogether = toInt(row[0]);
        int winsTogether = toInt(row[1]);
        return playerAnalyticsMapper.toPartnershipStats(matchesTogether, winsTogether);
    }

    private HeadToHeadResponse mapHeadToHead(List<Object[]> results) {
        if (results.isEmpty() || ObjectUtil.isNull(results.getFirst()[0])) {
            return playerAnalyticsMapper.emptyHeadToHead();
        }
        Object[] row = results.getFirst();
        return playerAnalyticsMapper.toHeadToHead(
                toInt(row[0]),
                toInt(row[1]),
                toInt(row[2]),
                toInt(row[3])
        );
    }

    private int toInt(Object value) {
        if (ObjectUtil.isNull(value)) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
