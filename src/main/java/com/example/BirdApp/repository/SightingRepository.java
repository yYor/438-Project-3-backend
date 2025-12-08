package com.example.BirdApp.repository;

import com.example.BirdApp.domain.Sighting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SightingRepository extends JpaRepository<Sighting, Long> {

    // all sightings for a given user
    List<Sighting> findByUserUserId(Long userId);

    // all sightings for a given bird (via birdId)
    List<Sighting> findByBirdBirdId(Long birdId);

    // recent sightings, for "Recent Sightings" section
    List<Sighting> findTop20ByOrderByObservedAtDesc();

    // sightings within a bounding box (for map)
    List<Sighting> findByLatitudeBetweenAndLongitudeBetween(
            Double minLat,
            Double maxLat,
            Double minLon,
            Double maxLon
    );

    // sightings in a time range (optional, but handy for filters)
    List<Sighting> findByObservedAtBetween(Instant start, Instant end);
}
