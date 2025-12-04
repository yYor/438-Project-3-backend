package com.example.BirdApp.repository;

import com.example.BirdApp.domain.Bird;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

@Repository
public interface BirdRepository extends JpaRepository<Bird, Long> {
   // @Query(value ="select * from \"birds\"", nativeQuery = true)
   // public Optional<List<Bird>> getAllBirds(); 
}