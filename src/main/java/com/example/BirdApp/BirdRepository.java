package com.example.BirdApp;

import com.example.BirdApp.domain.Bird;
import java.util.List;

public interface BirdRepository extends JpaRepository<Bird, Long> {

}