package com.example.BirdApp.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BirdApp.domain.Bird;
import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.BirdRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/birds")
public class BirdController {

    private final BirdRepository birdRepository;

    public BirdController(BirdRepository birdRepository) {
        this.birdRepository = birdRepository;
    }

    @GetMapping public List<Bird> all() { return birdRepository.findAll(); }
}