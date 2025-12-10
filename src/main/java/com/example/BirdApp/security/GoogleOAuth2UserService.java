package com.example.BirdApp.security;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Map;
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
    // (Feel free to rename this to CustomOAuth2UserService later; the bean name doesn't matter.)

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuth2UserService.class);
    private final UserRepository userRepository;

    public GoogleOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId(); // "google" or "github"
        Map<String, Object> attributes = oauthUser.getAttributes();
        logger.info("OAuth2 login with provider: {}", provider);
        logger.info("OAuth2User attributes: {}", attributes);

        // This is the provider's "primary key" for the user:
        // Google: "sub", GitHub: "id" (because of user-name-attribute=id)
        String oauthId = oauthUser.getName();

        String email;
        String name;
        String picture;

        if ("google".equalsIgnoreCase(provider)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");

            if (name == null || name.isEmpty()) {
                name = email;
            }

        } else if ("github".equalsIgnoreCase(provider)) {
            // GitHub: id, login, name, avatar_url, email...
            email = (String) attributes.get("email");
            String login = (String) attributes.get("login");

            if (email == null || email.isEmpty()) {
                // fallback so we have *some* stable email-like string
                email = login + "@github.local";
                logger.warn("GitHub did not provide email; using pseudo email: {}", email);
            }

            name = (String) attributes.get("name");
            if (name == null || name.isEmpty()) {
                name = login; // fallback to username
            }

            picture = (String) attributes.get("avatar_url");

        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth provider: " + provider);
        }

        if (email == null || email.isEmpty()) {
            logger.error("Email is null or empty after mapping for provider {}. Attributes: {}", provider, attributes);
            throw new OAuth2AuthenticationException("Email not provided by OAuth provider");
        }

        try {
            Optional<User> optionalUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);

            if (optionalUser.isEmpty()) {
                logger.info("Creating new user for provider={}, oauthId={}, email={}", provider, oauthId, email);

                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setProfilePicture(picture);
                newUser.setOauthProvider(provider);
                newUser.setOauthId(oauthId);
                newUser.setRole("user");

                User savedUser = userRepository.save(newUser);
                logger.info("Successfully created user with ID: {} for email: {}", savedUser.getUserId(), email);

            } else {
                User existing = optionalUser.get();
                logger.info("Found existing user {} for provider={}, oauthId={}",
                        existing.getUserId(), provider, oauthId);

                boolean updated = false;

                if (existing.getEmail() == null || existing.getEmail().isEmpty()) {
                    existing.setEmail(email);
                    updated = true;
                }
                if (existing.getName() == null || existing.getName().isEmpty()) {
                    existing.setName(name);
                    updated = true;
                }
                if (picture != null && !picture.isEmpty()
                        && (existing.getProfilePicture() == null || existing.getProfilePicture().isEmpty())) {
                    existing.setProfilePicture(picture);
                    updated = true;
                }
                if (existing.getOauthProvider() == null || existing.getOauthProvider().isEmpty()) {
                    existing.setOauthProvider(provider);
                    updated = true;
                }
                if (existing.getOauthId() == null || existing.getOauthId().isEmpty()) {
                    existing.setOauthId(oauthId);
                    updated = true;
                }

                if (updated) {
                    userRepository.save(existing);
                    logger.info("Updated user {} after OAuth login", existing.getUserId());
                }
            }
        } catch (Exception e) {
            logger.error("Error saving user for provider={}, oauthId={}, email={}", provider, oauthId, email, e);
            throw new OAuth2AuthenticationException("Failed to save user: " + e.getMessage());
        }

        return oauthUser;
    }
}
