package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.dto.tournament.SyncMatchResultRequest;
import com.amigos.courtpulse.dto.tournament.SyncMatchResultResponse;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.entity.TournamentMatch;
import com.amigos.courtpulse.entity.TournamentMatchPlayer;
import com.amigos.courtpulse.entity.TournamentMetadata;
import com.amigos.courtpulse.enums.TournamentStatusEnum;
import com.amigos.courtpulse.exception.PlayerNotFoundException;
import com.amigos.courtpulse.exception.TournamentNotFoundException;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.repository.TournamentMatchPlayerRepository;
import com.amigos.courtpulse.repository.TournamentMatchRepository;
import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import com.amigos.courtpulse.service.TournamentMatchService;
import com.amigos.courtpulse.util.ObjectUtil;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TournamentMatchServiceImpl implements TournamentMatchService {

    private static final Logger log = LoggerFactory.getLogger(TournamentMatchServiceImpl.class);

    private final TournamentMetadataRepository tournamentMetadataRepository;
    private final TournamentMatchRepository tournamentMatchRepository;
    private final TournamentMatchPlayerRepository tournamentMatchPlayerRepository;
    private final PlayerRepository playerRepository;

    @Override
    @Transactional
    public SyncMatchResultResponse syncMatchResult(Long tournamentId, SyncMatchResultRequest request) {
        log.info("Starting match synchronization for tournament ID: {}, round: {}, matchNo: {}", 
                tournamentId, request.roundName(), request.matchNo());

        // 1. Validate tournament exists
        TournamentMetadata tournament = tournamentMetadataRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        // 2. Auto-transition status to IN_PROGRESS if in a preliminary status (DRAFT, REGISTRATION_OPEN, REGISTRATION_CLOSED)
        if (ObjectUtil.isNotEqual(tournament.getStatus(), TournamentStatusEnum.IN_PROGRESS)) {
            TournamentStatusEnum currentStatus = tournament.getStatus();
            if (currentStatus == TournamentStatusEnum.DRAFT ||
                    currentStatus == TournamentStatusEnum.REGISTRATION_OPEN ||
                    currentStatus == TournamentStatusEnum.REGISTRATION_CLOSED) {
                tournament.setStatus(TournamentStatusEnum.IN_PROGRESS);
                tournamentMetadataRepository.save(tournament);
                log.info("Tournament ID: {} transitioned from {} to IN_PROGRESS", tournamentId, currentStatus);
            } else {
                throw new IllegalArgumentException("Cannot sync matches. Tournament status is not IN_PROGRESS (current: " + currentStatus + ")");
            }
        }

        // 3. Validate duplicate player IDs in request
        Set<Long> uniquePlayerIds = new HashSet<>();
        for (SyncMatchResultRequest.MatchPlayerRequest playerReq : request.players()) {
            if (!uniquePlayerIds.add(playerReq.playerId())) {
                throw new IllegalArgumentException("Duplicate player ID detected in match request: " + playerReq.playerId());
            }
        }

        // 4. Create and save tournament match
        TournamentMatch match = TournamentMatch.builder()
                .tournament(tournament)
                .roundName(request.roundName())
                .matchNo(request.matchNo())
                .scoreA(request.scoreA())
                .scoreB(request.scoreB())
                .winnerSide(request.winnerSide())
                .completedAt(request.completedAt())
                .analyticsProcessedAt(null)
                .build();

        TournamentMatch savedMatch = tournamentMatchRepository.save(match);
        log.info("Saved tournament match ID: {} for tournament ID: {}", savedMatch.getId(), tournamentId);

        // 5. Create and save match players
        for (SyncMatchResultRequest.MatchPlayerRequest playerReq : request.players()) {
            Player player = playerRepository.findById(playerReq.playerId())
                    .orElseThrow(() -> new PlayerNotFoundException(playerReq.playerId()));

            TournamentMatchPlayer matchPlayer = TournamentMatchPlayer.builder()
                    .tournamentMatch(savedMatch)
                    .player(player)
                    .side(playerReq.side())
                    .isWinner(playerReq.winner())
                    .build();

            tournamentMatchPlayerRepository.save(matchPlayer);
        }

        log.info("Successfully synchronized match ID: {} with {} players", savedMatch.getId(), request.players().size());
        return new SyncMatchResultResponse(savedMatch.getId());
    }
}
