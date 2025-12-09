package com.example.BirdApp;

import com.example.BirdApp.domain.Bird;
import com.example.BirdApp.domain.Sighting;
import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.BirdRepository;
import com.example.BirdApp.repository.SightingRepository;
import com.example.BirdApp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SightingService {

    private final SightingRepository sightingRepository;
    private final BirdRepository birdRepository;
    private final UserRepository userRepository;

    public SightingService(SightingRepository sightingRepository,
                           BirdRepository birdRepository,
                           UserRepository userRepository) {
        this.sightingRepository = sightingRepository;
        this.birdRepository = birdRepository;
        this.userRepository = userRepository;
    }

    public List<Sighting> getAllSightings() {
        return sightingRepository.findAll();
    }

    public List<Sighting> getRecentSightings() {
        return sightingRepository.findTop20ByOrderByObservedAtDesc();
    }

    public List<Sighting> getSightingsByBird(Long birdId) {
        return sightingRepository.findByBirdBirdId(birdId);
    }

    public List<Sighting> getSightingsByUser(Long userId) {
        return sightingRepository.findByUserUserId(userId);
    }

    public Sighting createSighting(Long userId,
                                   Long birdId,
                                   Integer count,
                                   Double latitude,
                                   Double longitude,
                                   String location,
                                   String notes,
                                   Instant observedAt) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Bird bird = birdRepository.findById(birdId)
                .orElseThrow(() -> new IllegalArgumentException("Bird not found"));

        Sighting s = new Sighting();
        s.setUser(user);
        s.setBird(bird);
        s.setCount(count);
        s.setLatitude(latitude);
        s.setLongitude(longitude);
        s.setLocation(location);
        s.setNotes(notes);
        s.setObservedAt(observedAt != null ? observedAt : Instant.now());

        return sightingRepository.save(s);
    }
}
