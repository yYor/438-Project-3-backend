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
        String name  = oauthUser.getAttribute("name");

        // If user does not exist, create them
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);

            // COMPLETE OAUTH USER DATA
            user.setOauthProvider("google");
            user.setOauthId(oauthUser.getName());  // Google's unique user ID (sub)
            user.setProfilePicture(oauthUser.getAttribute("picture"));
            user.setRole("USER");  // default role


            userRepository.save(user);
        }

        return oauthUser;
    }
}
