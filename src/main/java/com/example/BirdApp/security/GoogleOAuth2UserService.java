package com.example.BirdApp.security;
import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                // redirect user to "Signup Required" page
                throw new OAuth2AuthenticationException("User not registered");
            }

            return oauthUser;
        }
}
