package com.example.BirdApp.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BirdApp.domain.Bird;
import org.springframework.stereotype.Repository;

@Repository
public interface BirdRepository extends JpaRepository<Bird, Long> {

}