package com.knowzone.controller;

import com.knowzone.dto.LocationUpdateRequest;
import com.knowzone.dto.MatchResponseRequest;
import com.knowzone.persistence.entity.Match;
import com.knowzone.service.LocationService;
import com.knowzone.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LocationMatchingController {
    private final LocationService locationService;
    private final MatchingService matchingService;

    @PostMapping("/location/update")
    public ResponseEntity<?> updateLocation(@RequestBody LocationUpdateRequest request) {
        try {
            locationService.updateUserLocation(request.getLatitude(), request.getLongitude());
            return ResponseEntity.ok("Location updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating location: " + e.getMessage());
        }
    }

    @GetMapping("/matches/{userId}")
    public ResponseEntity<List<Match>> getUserMatches(@PathVariable Long userId) {
        return ResponseEntity.ok(matchingService.getUserMatches(userId));
    }

    @GetMapping("/matches/{userId}/accepted")
    public ResponseEntity<List<Match>> getUserAcceptedMatches(@PathVariable Long userId) {
        return ResponseEntity.ok(matchingService.getUserAcceptedMatches(userId));
    }

    @GetMapping("/matches/detail/{matchId}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchingService.getMatchById(matchId));
    }

    @PostMapping("/matches/{matchId}/respond")
    public ResponseEntity<?> respondToMatch(@PathVariable Long matchId,
                                            @RequestBody MatchResponseRequest request) {
        return ResponseEntity.ok(matchingService.respondToMatch(matchId,request));
    }

}
