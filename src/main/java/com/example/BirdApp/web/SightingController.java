package com.example.BirdApp.web;

import com.example.BirdApp.SightingService;
import com.example.BirdApp.domain.Sighting;
import com.example.BirdApp.dto.SightingRequest;
import com.example.BirdApp.dto.SightingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sightings")
@CrossOrigin // adjust origins if you want to restrict
public class SightingController {

    private final SightingService sightingService;

    @Autowired
    public SightingController(SightingService sightingService) {
        this.sightingService = sightingService;
    }

    // GET /api/sightings
    @GetMapping
    public List<SightingResponse> getAllSightings() {
        List<Sighting> sightings = sightingService.getAllSightings();
        return sightings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET /api/sightings/recent
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentSightings() {
        try {
            List<Sighting> sightings = sightingService.getRecentSightings();
            List<SightingResponse> responses = sightings.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            // Log full stack trace to Heroku logs
            e.printStackTrace();

            // Return JSON instead of HTML whitelabel page
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("error", e.getClass().getSimpleName());
            body.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    // GET /api/sightings/by-bird/{birdId}
    @GetMapping("/by-bird/{birdId}")
    public List<SightingResponse> getSightingsByBird(@PathVariable Long birdId) {
        List<Sighting> sightings = sightingService.getSightingsByBird(birdId);
        return sightings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET /api/sightings/by-user/{userId}
    @GetMapping("/by-user/{userId}")
    public List<SightingResponse> getSightingsByUser(@PathVariable Long userId) {
        List<Sighting> sightings = sightingService.getSightingsByUser(userId);
        return sightings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // POST /api/sightings
    @PostMapping
    public ResponseEntity<SightingResponse> createSighting(@RequestBody SightingRequest request) {
        Sighting created = sightingService.createSighting(
                request.getUserId(),
                request.getBirdId(),
                request.getCount(),
                request.getLatitude(),
                request.getLongitude(),
                request.getLocation(),
                request.getNotes(),
                request.getObservedAt()
        );

        return new ResponseEntity<>(toResponse(created), HttpStatus.CREATED);
    }

    // --- helper mapper ---

    private SightingResponse toResponse(Sighting s) {
        SightingResponse dto = new SightingResponse();
        dto.setId(s.getSightingId());

        if (s.getBird() != null) {
            dto.setBirdId(s.getBird().getBirdId());
            dto.setBirdName(s.getBird().getbirdName()); // if your Bird entity really has this exact getter
        }

        if (s.getUser() != null) {
            dto.setUserId(s.getUser().getUserId());
            dto.setUsername(s.getUser().getName());
        }

        dto.setCount(s.getCount());
        dto.setLatitude(s.getLatitude());
        dto.setLongitude(s.getLongitude());
        dto.setLocation(s.getLocation());
        dto.setNotes(s.getNotes());
        dto.setObservedAt(s.getObservedAt());

        return dto;
    }
}
