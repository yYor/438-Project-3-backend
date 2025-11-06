package com.example.BirdApp.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.util.List;

@Entity
@Table(name = "birds")
public class Bird {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "birdid")
    private Long birdId;

    //ex: Grey-headed chickadee 
    private String birdName;
    //ex: Poecile cinctus
    private String sciName;
    //ex: Alaska, Scandinavia: something else might work better but its a list for now, more descriptive locations might be better
    private List<String> habitat;
    //ex: Paridae, might be unecessary but i am covering our bases.
    private String family;
    //ex: Least Concern, Conservation status, again maybe unecessary but its here
    private String cnsrvStatus;


    public Bird() {}
    public Bird(String birdName, String sciName, String habitat, String family, String cnsrvStatus){
        this.birdName = birdName;
        this.sciName = sciName;
        this.habitat = habitat;
        this.family = family;
        this.cnrvStatus = cnsrvStatus;
    }

    // Getters and Setters
    public Long getBirdId() {
        return birdId;
    }
    public void setBirdId(Long birdId) {
        this.birdId = birdId;
    }
    public String getbirdName() {
        return birdName;
    }
    public void setbirdName(String birdName) {
        this.birdName = birdName;
    }
    public String getsciName() {
        return sciName;
    }
    public void setsciName(String sciName) {
        this.sciName = sciName;
    }
    public List<String> getHabitat() {
        return habitat;
    }
    public void setHabitat(List<String> habitat) {
        this.habitat = habitat;
    }
    public String getFamily() {
        return family;
    }
    public void setFamily(String family) {
        this.family = family;
    }
    public String getCnsrvStatus() {
        return cnsrvStatus;
    }
    public void setCnsrvStatus(String cnsrvStatus) {
        this.cnsrvStatus = cnsrvStatus;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}