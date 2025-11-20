package com.example.BirdApp.config;

import org.springframework.context.annotation.Bean;
import com.example.BirdApp.security.GoogleOAuth2UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final GoogleOAuth2UserService googleService;

    public SecurityConfig(GoogleOAuth2UserService googleService) {
        this.googleService = googleService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/public/**","/api/auth/signup","/oauth2/**","/login/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> {
                oauth.userInfoEndpoint(userInfo ->
                    userInfo.userService(googleService)
                );
                oauth.defaultSuccessUrl("/api/users/me", true);

                oauth.failureHandler((request, response, exception) -> {
                    // If it's your "User not registered" case, return 401 or redirect
                    if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException &&
                        "User not registered".equals(exception.getMessage())) {

                        // For API/mobile style response:
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"USER_NOT_REGISTERED\"}");
                    } else {
                        // generic failure
                        response.sendRedirect("/");
                    }
                });
            })
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
