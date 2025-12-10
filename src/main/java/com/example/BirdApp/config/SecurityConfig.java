package com.example.BirdApp.config;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;
import com.example.BirdApp.security.GoogleOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    private final GoogleOAuth2UserService oAuth2UserService;
    private final UserRepository userRepository;

    public SecurityConfig(GoogleOAuth2UserService oAuth2UserService, UserRepository userRepository) {
        this.oAuth2UserService = oAuth2UserService;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/signup").authenticated()
                .requestMatchers("/", "/api/public/**").permitAll()
                .requestMatchers("/api/birds/**").permitAll()
                .requestMatchers("/api/sightings/**").permitAll()
                .requestMatchers("/api/users/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> {
                oauth.userInfoEndpoint(userInfo ->
                    userInfo.userService(oAuth2UserService) // still your existing service bean
                );

                // defaultSuccessUrl won't really matter since we override successHandler,
                // but it doesn't hurt to keep it.
                oauth.defaultSuccessUrl("/", true);

                oauth.successHandler((request, response, authentication) -> {
                    var oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
                    var oauthUser  = (org.springframework.security.oauth2.core.user.OAuth2User) oauthToken.getPrincipal();

                    String provider = oauthToken.getAuthorizedClientRegistrationId(); // "google" or "github"
                    var attrs = oauthUser.getAttributes();

                    String email;
                    String name;
                    String picture;
                    String oauthId = oauthUser.getName(); // Google: "sub", GitHub: "id" (because of user-name-attribute)

                    if ("google".equalsIgnoreCase(provider)) {
                        // ---- Google mapping ----
                        email = (String) attrs.get("email");
                        name  = (String) attrs.get("name");
                        picture = (String) attrs.get("picture");

                        if (name == null || name.isEmpty()) {
                            name = email;
                        }

                    } else if ("github".equalsIgnoreCase(provider)) {
                        // ---- GitHub mapping ----
                        // GitHub gives: id, login, name, avatar_url, email (may be null)
                        email = (String) attrs.get("email");
                        String login = (String) attrs.get("login");

                        if (email == null || email.isBlank()) {
                            // fallback so we always have something stable
                            email = login + "@github.local";
                        }

                        name = (String) attrs.get("name");
                        if (name == null || name.isBlank()) {
                            name = login;
                        }

                        picture = (String) attrs.get("avatar_url");
                    } else {
                        // If some other provider appears, bail
                        response.sendRedirect("https://birdwatchers-c872a1ce9f02.herokuapp.com/error");
                        return;
                    }

                    if (email == null || email.isBlank()) {
                        response.sendRedirect("https://birdwatchers-c872a1ce9f02.herokuapp.com/error");
                        return;
                    }

                    // === This is basically your OLD logic, just with provider-aware mapping ===
                    User user = userRepository.findByEmail(email).orElseGet(User::new);

                    user.setEmail(email);
                    user.setName(name != null ? name : "");
                    user.setProfilePicture(picture);
                    user.setOauthProvider(provider);  // "google" or "github"
                    user.setOauthId(oauthId);        // Google's "sub" or GitHub's "id"
                    if (user.getRole() == null || user.getRole().isBlank()) {
                        user.setRole("user");
                    }

                    user = userRepository.save(user);

                    String redirectUrl =
                        "https://birdwatchers-c872a1ce9f02.herokuapp.com/api/auth/mobile-redirect" +
                        "?userId=" + user.getUserId() +
                        "&name=" + java.net.URLEncoder.encode(user.getName() != null ? user.getName() : "", java.nio.charset.StandardCharsets.UTF_8) +
                        "&email=" + java.net.URLEncoder.encode(user.getEmail(), java.nio.charset.StandardCharsets.UTF_8) +
                        "&picture=" + java.net.URLEncoder.encode(user.getProfilePicture() != null ? user.getProfilePicture() : "", java.nio.charset.StandardCharsets.UTF_8);

                    response.sendRedirect(redirectUrl);
                });

            })
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
