package com.example.BirdApp.domain;
// import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
// import java.time.Instant;
// import java.util.HashSet;
// import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "birds")
public class Bird {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"birdId\"")
    private Long birdId;

    @Column(name = "\"birdName\"")
    private String birdName;

    @Column(name = "\"sciName\"")
    private String sciName;

    @Column(name = "\"habitat\"")
    private String habitat;

    @Column(name = "\"family\"")
    private String family;

    @Column(name = "\"cnsrvStatus\"")
    private String cnsrvStatus;

    // NEW: long description text about the bird
    @Column(name = "\"description\"")
    private String description;

    // NEW: URL to a picture for this bird
    @Column(name = "\"pictureUrl\"")
    private String pictureUrl;

    public Bird() {}

    public Bird(String birdName,
                String sciName,
                String habitat,
                String family,
                String cnsrvStatus,
                String description,
                String pictureUrl) {
        this.birdName = birdName;
        this.sciName = sciName;
        this.habitat = habitat;
        this.family = family;
        this.cnsrvStatus = cnsrvStatus;
        this.description = description;
        this.pictureUrl = pictureUrl;
    }

    public Long getBirdId() { return birdId; }
    public void setBirdId(Long birdId) { this.birdId = birdId; }

    public String getBirdName() { return birdName; }
    public void setBirdName(String birdName) { this.birdName = birdName; }

    public String getSciName() { return sciName; }
    public void setSciName(String sciName) { this.sciName = sciName; }

    public String getHabitat() { return habitat; }
    public void setHabitat(String habitat) { this.habitat = habitat; }

    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }

    public String getCnsrvStatus() { return cnsrvStatus; }
    public void setCnsrvStatus(String cnsrvStatus) { this.cnsrvStatus = cnsrvStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bird bird = (Bird) o;
        return Objects.equals(birdId, bird.birdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(birdId);
    }
}