package com.example.BirdApp.config;

import org.springframework.context.annotation.Bean;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;
import com.example.BirdApp.security.GoogleOAuth2UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final GoogleOAuth2UserService googleService;
    private final UserRepository userRepository;

    public SecurityConfig(GoogleOAuth2UserService googleService, UserRepository userRepository) {
        this.googleService = googleService;
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
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> {
                oauth.userInfoEndpoint(userInfo ->
                    userInfo.userService(googleService)
                );

                // Required in Spring Boot 3 to avoid registrationId confusion
                oauth.defaultSuccessUrl("/", true);

                oauth.successHandler((request, response, authentication) -> {
                    var oauthUser = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();

                    String email   = oauthUser.<String>getAttribute("email");
                    String rawName = oauthUser.<String>getAttribute("name");
                    String rawPic  = oauthUser.<String>getAttribute("picture");
                    String sub     = oauthUser.getName();

                    if (email == null || email.isBlank()) {
                        response.sendRedirect("https://birdwatchers-c872a1ce9f02.herokuapp.com/error");
                        return;
                    }

                    String name    = rawName != null ? rawName : "";
                    String picture = rawPic  != null ? rawPic  : "";

                    User user = userRepository.findByEmail(email)
                        .orElseGet(() -> new User());

                    user.setEmail(email);
                    user.setName(name);
                    user.setProfilePicture(picture);
                    user.setOauthProvider("google");
                    user.setOauthId(sub);

                    user = userRepository.save(user);

                    String redirectUrl =
                        "438project3frontend://oauth2redirect" +
                        "?userId=" + user.getUserId() +
                        "&name=" + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8) +
                        "&email=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                        "&picture=" + java.net.URLEncoder.encode(picture, java.nio.charset.StandardCharsets.UTF_8);

                    response.sendRedirect(redirectUrl);
                });

                // (Optional) you can plug your failureHandler here again if you want
            })
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
