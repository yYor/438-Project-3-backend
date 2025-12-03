package com.example.BirdApp.security;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuth2UserService.class);
    private final UserRepository userRepository;

    public GoogleOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oauthUser = super.loadUser(request);

        // Log all available attributes for debugging
        logger.info("OAuth2User attributes: {}", oauthUser.getAttributes());

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");
        String sub = oauthUser.getAttribute("sub"); // Google's unique user ID

        // Validate required fields
        if (email == null || email.isEmpty()) {
            logger.error("Email is null or empty from OAuth response. Available attributes: {}",
                    oauthUser.getAttributes());
            throw new OAuth2AuthenticationException("Email not provided by OAuth provider");
        }

        // Use email as fallback for name if name is null
        if (name == null || name.isEmpty()) {
            name = email;
            logger.warn("Name not provided by OAuth, using email as name: {}", email);
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
                newUser.setRole("user");

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

        return oauthUser;
    }
}