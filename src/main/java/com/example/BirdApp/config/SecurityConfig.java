package com.example.BirdApp.config;

import org.springframework.context.annotation.Bean;
import com.example.BirdApp.security.GoogleOAuth2UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpSession;

@Configuration
public class SecurityConfig {
    private final GoogleOAuth2UserService googleService;

    private static final String MOBILE_SESSION_ATTR = "oauth2_mobile_flag";

    public SecurityConfig(GoogleOAuth2UserService googleService) {
        this.googleService = googleService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/signup").authenticated()
                        .requestMatchers("/", "/api/public/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> {
                    oauth.userInfoEndpoint(userInfo -> userInfo.userService(googleService));

                    // Custom authorization request repository to save mobile flag in session
                    oauth.authorizationEndpoint(authEndpoint -> {
                        authEndpoint.authorizationRequestRepository(
                                new AuthorizationRequestRepository<OAuth2AuthorizationRequest>() {
                                    private final HttpSessionOAuth2AuthorizationRequestRepository delegate = new HttpSessionOAuth2AuthorizationRequestRepository();

                                    @Override
                                    public OAuth2AuthorizationRequest loadAuthorizationRequest(
                                            jakarta.servlet.http.HttpServletRequest request) {
                                        return delegate.loadAuthorizationRequest(request);
                                    }

                                    @Override
                                    public void saveAuthorizationRequest(
                                            OAuth2AuthorizationRequest authorizationRequest,
                                            jakarta.servlet.http.HttpServletRequest request,
                                            jakarta.servlet.http.HttpServletResponse response) {
                                        // Save mobile flag to session before OAuth redirect
                                        String mobileFlag = request.getParameter("mobile");
                                        if ("true".equalsIgnoreCase(mobileFlag)) {
                                            request.getSession().setAttribute(MOBILE_SESSION_ATTR, true);
                                        }
                                        delegate.saveAuthorizationRequest(authorizationRequest, request, response);
                                    }

                                    @Override
                                    public OAuth2AuthorizationRequest removeAuthorizationRequest(
                                            jakarta.servlet.http.HttpServletRequest request,
                                            jakarta.servlet.http.HttpServletResponse response) {
                                        return delegate.removeAuthorizationRequest(request, response);
                                    }
                                });
                    });

                    oauth.successHandler((request, response, authentication) -> {
                        // TODO: replace with real JWT later if you want token-based auth
                        String token = "TEMP_TOKEN";

                        // Check session for mobile flag (survives OAuth redirect)
                        HttpSession session = request.getSession(false);
                        boolean fromMobileApp = session != null &&
                                Boolean.TRUE.equals(session.getAttribute(MOBILE_SESSION_ATTR));

                        // Clean up session attribute
                        if (session != null) {
                            session.removeAttribute(MOBILE_SESSION_ATTR);
                        }

                        if (fromMobileApp) {
                            // Mobile: send deep-link back to Expo app
                            String redirectUrl = "438project3frontend://oauth2redirect?token=" + token;
                            response.sendRedirect(redirectUrl);
                        } else {
                            // Browser: redirect to signup endpoint
                            response.sendRedirect("/api/auth/signup");
                        }
                    });
                })
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll());
        return http.build();
    }
}