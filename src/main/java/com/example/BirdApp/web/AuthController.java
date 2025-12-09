package com.example.BirdApp.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.dto.SignupRequest;
import com.example.BirdApp.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/signup")
    public ResponseEntity<?> signupFromOAuth(OAuth2AuthenticationToken auth) {
        // This endpoint handles OAuth redirects (GET requests)
        if (auth == null) {
            logger.warn("OAuth authentication token is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Not authenticated");
        }

        logger.info("OAuth principal attributes: {}", auth.getPrincipal().getAttributes());
        String email = auth.getPrincipal().getAttribute("email");
        logger.info("Looking up user with email: {}", email);

        if (email == null || email.isEmpty()) {
            logger.error("Email is null or empty from OAuth token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email not found in authentication token");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            logger.error("User not found in database for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found for email: " + email);
        }

        logger.info("Found user with ID: {} for email: {}", user.getUserId(), email);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        // This endpoint handles manual signups (POST requests)

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setOauthProvider(req.getOauthProvider()); // "google"
        user.setOauthId(req.getOauthId());
        user.setProfilePicture(req.getProfilePicture());

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/mobile-redirect")
    public void mobileRedirect(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {

        String base = "438project3frontend://oauth2redirect";

        String redirectUrl = base;

        if (!params.isEmpty()) {
            String query = params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            redirectUrl = base + "?" + query;
        }

        response.setStatus(302);
        response.setHeader("Location", redirectUrl);
    }
}