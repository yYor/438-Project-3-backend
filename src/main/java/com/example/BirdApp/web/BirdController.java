package com.example.BirdApp.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BirdApp.BirdService;

@RestController
@RequestMapping("/api/birds")
public class BirdController {
    
    BirdService birdService;
    
    @Autowired
    public BirdController(BirdService birdService) {
        this.birdService = birdService;
    }
    
    @GetMapping("/getBirds")
    public ResponseEntity<?> getBirds() {
        return new ResponseEntity<>(this.birdService.getAllBirds(), HttpStatus.OK);
    }

}