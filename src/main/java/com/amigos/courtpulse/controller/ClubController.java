package com.amigos.courtpulse.controller;

import com.amigos.courtpulse.dto.common.ApiResponse;
import com.amigos.courtpulse.dto.club.ClubJoinRequestResponse;
import com.amigos.courtpulse.dto.club.ClubMemberResponse;
import com.amigos.courtpulse.dto.club.ClubProfileResponse;
import com.amigos.courtpulse.dto.club.ClubSearchResponse;
import com.amigos.courtpulse.dto.club.CreateClubRequest;
import com.amigos.courtpulse.dto.club.LeaveClubResponse;
import com.amigos.courtpulse.dto.club.MyClubJoinRequestResponse;
import com.amigos.courtpulse.dto.club.MyClubResponse;
import com.amigos.courtpulse.service.ClubService;
import com.amigos.courtpulse.util.ResponseUtil;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClubProfileResponse>> createClub(
            @Valid @RequestBody CreateClubRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ClubProfileResponse response = clubService.createClub(request, playerCode(jwt));
        URI location = URI.create("/api/v1/clubs/" + response.clubCode());
        return ResponseUtil.created("Club created successfully", response, location);
    }

    @GetMapping("/{clubCode}")
    public ResponseEntity<ApiResponse<ClubProfileResponse>> getClubByCode(
            @PathVariable String clubCode
    ) {
        ClubProfileResponse response = clubService.getClubByCode(clubCode);
        return ResponseUtil.ok("Club profile fetched successfully", response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ClubSearchResponse>>> searchClubs(
            @RequestParam(name = "q", defaultValue = "") String query
    ) {
        List<ClubSearchResponse> response = clubService.searchClubs(query);
        return ResponseUtil.ok("Clubs fetched successfully", response);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MyClubResponse>>> getMyClubs(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<MyClubResponse> response = clubService.getMyClubs(playerCode(jwt));
        return ResponseUtil.ok("My clubs fetched successfully", response);
    }

    @GetMapping("/requests/my")
    public ResponseEntity<ApiResponse<List<MyClubJoinRequestResponse>>> getMyJoinRequests(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<MyClubJoinRequestResponse> response = clubService.getMyJoinRequests(playerCode(jwt));
        return ResponseUtil.ok("My club join requests fetched successfully", response);
    }

    @PostMapping("/{clubCode}/join")
    public ResponseEntity<ApiResponse<ClubJoinRequestResponse>> createJoinRequest(
            @PathVariable String clubCode,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ClubJoinRequestResponse response = clubService.createJoinRequest(clubCode, playerCode(jwt));
        URI location = URI.create("/api/v1/clubs/requests/" + response.requestId());
        return ResponseUtil.created("Club join request created successfully", response, location);
    }

    @PostMapping("/{clubCode}/leave")
    public ResponseEntity<ApiResponse<LeaveClubResponse>> leaveClub(
            @PathVariable String clubCode,
            @AuthenticationPrincipal Jwt jwt
    ) {
        LeaveClubResponse response = clubService.leaveClub(clubCode, playerCode(jwt));
        return ResponseUtil.ok("Left club successfully", response);
    }

    @GetMapping("/{clubCode}/requests")
    public ResponseEntity<ApiResponse<List<ClubJoinRequestResponse>>> getPendingJoinRequests(
            @PathVariable String clubCode,
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<ClubJoinRequestResponse> response = clubService.getPendingJoinRequests(
                clubCode,
                playerCode(jwt)
        );
        return ResponseUtil.ok("Club join requests fetched successfully", response);
    }

    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<ClubJoinRequestResponse>> approveJoinRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ClubJoinRequestResponse response = clubService.approveJoinRequest(requestId, playerCode(jwt));
        return ResponseUtil.ok("Club join request approved successfully", response);
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<ClubJoinRequestResponse>> rejectJoinRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ClubJoinRequestResponse response = clubService.rejectJoinRequest(requestId, playerCode(jwt));
        return ResponseUtil.ok("Club join request rejected successfully", response);
    }

    @GetMapping("/{clubCode}/members")
    public ResponseEntity<ApiResponse<List<ClubMemberResponse>>> getClubMembers(
            @PathVariable String clubCode
    ) {
        List<ClubMemberResponse> response = clubService.getClubMembers(clubCode);
        return ResponseUtil.ok("Club members fetched successfully", response);
    }

    private String playerCode(Jwt jwt) {
        return jwt.getClaimAsString("playerCode");
    }
}
