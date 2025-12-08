package com.example.BirdApp.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "sightings")
public class Sighting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sightingid")
    private Long sightingId;

    // link to Bird via birdid FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birdid", nullable = false)
    private Bird bird;

    // link to User via userid FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    private Integer count;

    private Double latitude;
    private Double longitude;

    // human-readable place like "Point Pinos, Pacific Grove"
    private String location;

    @Column(length = 1000)
    private String notes;

    // when the bird was seen
    @Column(name = "observed_at")
    private Instant observedAt;

    // --- getters & setters ---

    public Long getSightingId() {
        return sightingId;
    }

    public void setSightingId(Long sightingId) {
        this.sightingId = sightingId;
    }

    public Bird getBird() {
        return bird;
    }

    public void setBird(Bird bird) {
        this.bird = bird;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }

    // equals / hashCode on ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sighting)) return false;
        Sighting sighting = (Sighting) o;
        return Objects.equals(sightingId, sighting.sightingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sightingId);
    }
}
