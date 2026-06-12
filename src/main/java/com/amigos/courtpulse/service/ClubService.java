package com.amigos.courtpulse.service;

import com.amigos.courtpulse.dto.club.ClubJoinRequestResponse;
import com.amigos.courtpulse.dto.club.ClubMemberResponse;
import com.amigos.courtpulse.dto.club.ClubProfileResponse;
import com.amigos.courtpulse.dto.club.ClubSearchResponse;
import com.amigos.courtpulse.dto.club.CreateClubRequest;
import com.amigos.courtpulse.dto.club.LeaveClubResponse;
import com.amigos.courtpulse.dto.club.MyClubJoinRequestResponse;
import com.amigos.courtpulse.dto.club.MyClubResponse;
import java.util.List;

public interface ClubService {

    ClubProfileResponse createClub(CreateClubRequest request, String ownerPlayerCode);

    ClubProfileResponse getClubByCode(String clubCode);

    List<ClubSearchResponse> searchClubs(String query);

    List<MyClubResponse> getMyClubs(String playerCode);

    List<MyClubJoinRequestResponse> getMyJoinRequests(String playerCode);

    ClubJoinRequestResponse createJoinRequest(String clubCode, String playerCode);

    List<ClubJoinRequestResponse> getPendingJoinRequests(String clubCode, String reviewerPlayerCode);

    ClubJoinRequestResponse approveJoinRequest(Long requestId, String reviewerPlayerCode);

    ClubJoinRequestResponse rejectJoinRequest(Long requestId, String reviewerPlayerCode);

    LeaveClubResponse leaveClub(String clubCode, String playerCode);

    List<ClubMemberResponse> getClubMembers(String clubCode);
}
