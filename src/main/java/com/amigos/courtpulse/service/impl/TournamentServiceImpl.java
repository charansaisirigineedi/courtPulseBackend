package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.dto.tournament.CreateTournamentRequest;
import com.amigos.courtpulse.dto.tournament.CreateTournamentResponse;
import com.amigos.courtpulse.dto.tournament.TournamentResponse;
import com.amigos.courtpulse.entity.Club;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.entity.TournamentMetadata;
import com.amigos.courtpulse.enums.TournamentStatusEnum;
import com.amigos.courtpulse.exception.ClubAccessDeniedException;
import com.amigos.courtpulse.exception.ClubNotFoundException;
import com.amigos.courtpulse.exception.PlayerNotFoundException;
import com.amigos.courtpulse.exception.TournamentNotFoundException;
import com.amigos.courtpulse.mapper.TournamentMapper;
import com.amigos.courtpulse.repository.ClubRepository;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.repository.TournamentMetadataRepository;
import com.amigos.courtpulse.service.CacheEvictionService;
import com.amigos.courtpulse.service.TournamentService;
import com.amigos.courtpulse.util.CacheNames;
import com.amigos.courtpulse.util.ObjectUtil;
import com.amigos.courtpulse.util.TournamentCodeGenerator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private static final Logger log = LoggerFactory.getLogger(TournamentServiceImpl.class);

    private final TournamentMetadataRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final TournamentCodeGenerator tournamentCodeGenerator;
    private final TournamentMapper tournamentMapper;
    private final CacheEvictionService cacheEvictionService;

    @Override
    @Transactional
    public CreateTournamentResponse createTournament(CreateTournamentRequest request, String playerCode) {
        Player player = findPlayerByCode(playerCode);
        Club club = findClubById(request.clubId());

        // Validate player is the owner of the club
        if (ObjectUtil.isNotEqual(club.getOwner().getId(), player.getId())) {
            throw new ClubAccessDeniedException(player.getPlayerCode(), club.getId());
        }

        TournamentMetadata tournament = TournamentMetadata.builder()
                .tournamentCode(tournamentCodeGenerator.generateUniqueCode())
                .club(club)
                .tournamentName(normalizeRequired(request.tournamentName()))
                .description(normalizeOptional(request.description()))
                .tournamentType(request.tournamentType())
                .status(TournamentStatusEnum.DRAFT)
                .tournamentDate(request.tournamentDate())
                .registrationStartAt(request.registrationStartAt())
                .registrationEndAt(request.registrationEndAt())
                .createdBy(player)
                .build();

        TournamentMetadata savedTournament = tournamentRepository.save(tournament);
        cacheEvictionService.evictClubTournaments(club.getId(), club.getClubCode());
        log.info("Created tournament {} for club ID {} by player {}",
                savedTournament.getTournamentCode(), club.getId(), player.getPlayerCode());

        return tournamentMapper.toCreateResponse(savedTournament);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.TOURNAMENT_DETAILS, key = "#id")
    public TournamentResponse getTournamentById(Long id) {
        TournamentMetadata tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException(id));
        return tournamentMapper.toResponse(tournament);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.CLUB_TOURNAMENTS,
            key = "T(com.amigos.courtpulse.util.CacheKeys).clubIdentifier(#clubIdentifier)"
    )
    public List<TournamentResponse> getTournamentsByClubIdentifier(String clubIdentifier) {
        Club club;
        if (normalizeRequired(clubIdentifier).matches("^\\d+$")) {
            Long clubId = Long.parseLong(clubIdentifier);
            club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new ClubNotFoundException(clubId));
        } else {
            String normalized = normalizeRequired(clubIdentifier).toUpperCase(Locale.ROOT);
            club = clubRepository.findByClubCode(normalized)
                    .orElseThrow(() -> new ClubNotFoundException(normalized));
        }
        
        return tournamentRepository.findAllByClubIdOrderByTournamentDateDesc(club.getId())
                .stream()
                .map(tournamentMapper::toResponse)
                .toList();
    }

    private Player findPlayerByCode(String playerCode) {
        String normalized = normalizeRequired(playerCode).toUpperCase(Locale.ROOT);
        return playerRepository.findByPlayerCode(normalized)
                .orElseThrow(() -> new PlayerNotFoundException(normalized));
    }

    private Club findClubById(Long clubId) {
        if (ObjectUtil.isNull(clubId)) {
            throw new IllegalArgumentException("Club ID must not be null");
        }
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));
    }

    private String normalizeRequired(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Required value must not be blank");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
