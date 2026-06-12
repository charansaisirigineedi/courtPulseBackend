package com.amigos.courtpulse.mapper;

import com.amigos.courtpulse.dto.club.ClubJoinRequestResponse;
import com.amigos.courtpulse.dto.club.ClubMemberResponse;
import com.amigos.courtpulse.dto.club.ClubProfileResponse;
import com.amigos.courtpulse.dto.club.ClubSearchResponse;
import com.amigos.courtpulse.dto.club.MyClubJoinRequestResponse;
import com.amigos.courtpulse.dto.club.MyClubResponse;
import com.amigos.courtpulse.entity.Club;
import com.amigos.courtpulse.entity.ClubJoinRequest;
import com.amigos.courtpulse.entity.ClubMember;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.util.ObjectUtil;
import org.springframework.stereotype.Component;

@Component
public class ClubMapper {

    public ClubProfileResponse toProfileResponse(Club club) {
        Player owner = club.getOwner();
        return new ClubProfileResponse(
                club.getId(),
                club.getClubCode(),
                club.getClubName(),
                club.getDescription(),
                owner.getPlayerCode(),
                owner.getName(),
                club.getStatus(),
                club.getCreatedAt()
        );
    }

    public ClubSearchResponse toSearchResponse(Club club) {
        return new ClubSearchResponse(
                club.getClubCode(),
                club.getClubName(),
                club.getDescription()
        );
    }

    public ClubMemberResponse toMemberResponse(ClubMember clubMember) {
        return new ClubMemberResponse(
                clubMember.getClub().getClubCode(),
                clubMember.getPlayer().getPlayerCode(),
                clubMember.getPlayer().getName(),
                clubMember.getRole(),
                clubMember.getJoinedAt()
        );
    }

    public MyClubResponse toMyClubResponse(ClubMember clubMember) {
        return new MyClubResponse(
                clubMember.getClub().getClubCode(),
                clubMember.getClub().getClubName(),
                clubMember.getClub().getDescription(),
                clubMember.getRole(),
                clubMember.getClub().getStatus(),
                clubMember.getClub().getCreatedAt()
        );
    }

    public MyClubJoinRequestResponse toMyJoinRequestResponse(ClubJoinRequest request) {
        return new MyClubJoinRequestResponse(
                request.getId(),
                request.getClub().getClubCode(),
                request.getClub().getClubName(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    public ClubJoinRequestResponse toJoinRequestResponse(ClubJoinRequest request) {
        Player reviewer = request.getReviewedBy();
        return new ClubJoinRequestResponse(
                request.getId(),
                request.getClub().getClubCode(),
                request.getClub().getClubName(),
                request.getPlayer().getPlayerCode(),
                request.getPlayer().getName(),
                request.getStatus(),
                ObjectUtil.isNull(reviewer) ? null : reviewer.getPlayerCode(),
                request.getReviewedAt(),
                request.getCreatedAt()
        );
    }
}
