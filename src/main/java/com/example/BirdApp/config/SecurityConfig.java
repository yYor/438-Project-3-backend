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
                    userInfo.userService(oAuth2UserService) // now handles google + github
                );

                oauth.defaultSuccessUrl("/", true);

                oauth.successHandler((request, response, authentication) -> {
                    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                    OAuth2User oauthUser = oauthToken.getPrincipal();

                    String provider = oauthToken.getAuthorizedClientRegistrationId(); // "google" or "github"
                    String oauthId = oauthUser.getName(); // same id we used in the service

                    // Find the user that the service just created/updated
                    User user = userRepository
                        .findByOauthProviderAndOauthId(provider, oauthId)
                        .orElseThrow(() -> new IllegalStateException(
                            "User not found after OAuth2 login for provider=" + provider + ", oauthId=" + oauthId
                        ));

                    String name = user.getName() != null ? user.getName() : "";
                    String email = user.getEmail();
                    String picture = user.getProfilePicture() != null ? user.getProfilePicture() : "";

                    if (email == null || email.isBlank()) {
                        // This should be rare but let's still guard it
                        response.sendRedirect("https://birdwatchers-c872a1ce9f02.herokuapp.com/error");
                        return;
                    }

                    String redirectUrl =
                        "https://birdwatchers-c872a1ce9f02.herokuapp.com/api/auth/mobile-redirect" +
                        "?userId=" + user.getUserId() +
                        "&name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) +
                        "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
                        "&picture=" + URLEncoder.encode(picture, StandardCharsets.UTF_8);

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
