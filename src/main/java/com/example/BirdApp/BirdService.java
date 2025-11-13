package com.example.BirdApp;

import java.util.List;
import com.example.BirdApp.domain.Bird;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.BirdApp.repository.BirdRepository;

@Service
public class BirdService {
    private final BirdRepository repo;
    
    @Autowired
    public BirdService(BirdRepository repo) {
        this.repo = repo;
    }
    
    public List<Bird> getAllBirds() {
        return repo.getAllBirds().get();
    }
}