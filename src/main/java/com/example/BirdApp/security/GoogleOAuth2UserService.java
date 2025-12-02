package com.example.BirdApp.security;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public GoogleOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oauthUser = super.loadUser(request);
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");
        String sub = oauthUser.getAttribute("sub"); // Google's unique user ID

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            // Automatically create user from OAuth data
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name != null ? name : email);
            newUser.setOauthProvider("google");
            newUser.setOauthId(sub);
            newUser.setProfilePicture(picture);
            userRepository.save(newUser);
        } else {
            // Update existing user's OAuth info if needed
            User existingUser = optionalUser.get();
            if (existingUser.getOauthId() == null || existingUser.getOauthId().isEmpty()) {
                existingUser.setOauthProvider("google");
                existingUser.setOauthId(sub);
                if (picture != null) {
                    existingUser.setProfilePicture(picture);
                }
                userRepository.save(existingUser);
            }
        }

        return oauthUser;
    }
}








