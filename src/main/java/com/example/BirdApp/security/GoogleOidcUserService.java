package com.example.BirdApp.security;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleOidcUserService extends OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOidcUserService.class);
    private final UserRepository userRepository;

    public GoogleOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest request) {
        logger.info("=== GoogleOidcUserService.loadUser() CALLED ===");

        OidcUser oidcUser = super.loadUser(request);

        // Log all available attributes for debugging
        logger.info("OidcUser attributes: {}", oidcUser.getAttributes());

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();
        String sub = oidcUser.getSubject(); // Google's unique user ID

        logger.info("Extracted - email: {}, name: {}, sub: {}", email, name, sub);

        // Validate required fields
        if (email == null || email.isEmpty()) {
            logger.error("Email is null or empty from OIDC response. Available attributes: {}",
                    oidcUser.getAttributes());
            throw new OAuth2AuthenticationException("Email not provided by OAuth provider");
        }

        // Use email as fallback for name if name is null
        if (name == null || name.isEmpty()) {
            name = email;
            logger.warn("Name not provided by OIDC, using email as name: {}", email);
        }

        try {
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                // Automatically create user from OAuth data
                logger.info("Creating new user for email: {}", email);
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setOauthProvider("google");
                newUser.setOauthId(sub != null ? sub : "");
                newUser.setProfilePicture(picture);

                User savedUser = userRepository.save(newUser);
                logger.info("Successfully created user with ID: {} for email: {}", savedUser.getUserId(), email);
            } else {
                // Update existing user's OAuth info if needed
                User existingUser = optionalUser.get();
                logger.info("Found existing user with ID: {} for email: {}", existingUser.getUserId(), email);

                if (existingUser.getOauthId() == null || existingUser.getOauthId().isEmpty()) {
                    existingUser.setOauthProvider("google");
                    existingUser.setOauthId(sub != null ? sub : "");
                    if (picture != null) {
                        existingUser.setProfilePicture(picture);
                    }
                    userRepository.save(existingUser);
                    logger.info("Updated OAuth info for existing user: {}", email);
                }
            }
        } catch (Exception e) {
            logger.error("Error saving user for email: {}", email, e);
            throw new OAuth2AuthenticationException("Failed to save user: " + e.getMessage());
        }

        logger.info("=== GoogleOidcUserService.loadUser() COMPLETED SUCCESSFULLY ===");
        return oidcUser;
    }
}