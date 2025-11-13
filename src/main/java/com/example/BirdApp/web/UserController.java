package com.example.BirdApp.web;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController @RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) { this.repo = repo; }

    @GetMapping public List<User> all() { return repo.findAll(); }

    @GetMapping("/me")
    public User getCurrentUser(OAuth2AuthenticationToken auth) {
        String email = auth.getPrincipal().getAttribute("email");
        return repo.findByEmail(email).orElse(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable("id") Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody User u) {
        if (repo.existsByEmail(u.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Email already exists");
        }
        User saved = repo.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User updated) {
        return repo.findById(id).map(existing -> {
            existing.setOauthProvider(updated.getOauthProvider());
            existing.setOauthId(updated.getOauthId());
            existing.setName(updated.getName());
            User saved = repo.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) { 
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id); 
        return ResponseEntity.noContent().build();
    }

    

    
}
