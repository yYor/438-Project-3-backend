package com.example.BirdApp;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BirdApp.domain.Bird;

public interface BirdRepository extends JpaRepository<Bird, Long> {

}